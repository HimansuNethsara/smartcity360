package lk.jiat.smartcity360.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import lk.jiat.smartcity360.domain.ReportJob;
import lk.jiat.smartcity360.domain.User;
import lk.jiat.smartcity360.domain.enums.ReportStatus;
import lk.jiat.smartcity360.domain.enums.ReportType;
import lk.jiat.smartcity360.dto.ReportJobView;
import lk.jiat.smartcity360.repository.ReportJobRepository;
import lk.jiat.smartcity360.repository.UserRepository;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Stateless
public class ReportJobService {

    public record JobSpec(long id, long userId, ReportType type, int hours) {
    }

    private static final int DEFAULT_HOURS = 24;
    private static final int MAX_HOURS = 168;

    @EJB
    private ReportJobRepository jobRepository;
    @EJB
    private UserRepository userRepository;

    // ---------------------------------------------------------------- request thread

    public ReportJobView submit(Long userId, ReportType type, Integer hours) {
        int h = hours == null ? DEFAULT_HOURS : hours;
        if (h < 1 || h > MAX_HOURS) {
            throw new BusinessRuleException("hours must be between 1 and " + MAX_HOURS);
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User " + userId + " not found");
        }
        ReportJob job = ReportJob.builder()
                .requestedBy(user)
                .reportType(type)
                .paramsJson("{\"hours\":" + h + "}")
                .build();
        jobRepository.save(job);
        return view(job, false);
    }

    public List<ReportJobView> listOwn(Long userId) {
        return jobRepository.findByRequester(userId).stream().limit(50).map(j -> view(j, false)).toList();
    }

    public ReportJobView getOwn(Long id, Long userId) {
        ReportJob job = jobRepository.findById(id);
        if (job == null || !job.getRequestedBy().getId().equals(userId)) {
            throw new ResourceNotFoundException("Report " + id + " not found");
        }
        return view(job, true);
    }

    // ---------------------------------------------------------------- worker thread

    public JobSpec markRunning(Long id) {
        ReportJob job = jobRepository.findById(id);
        if (job == null || job.getStatus() != ReportStatus.QUEUED) {
            return null;                                   // gone, or already picked up
        }
        job.setStatus(ReportStatus.RUNNING);
        return new JobSpec(id, job.getRequestedBy().getId(), job.getReportType(), hoursOf(job.getParamsJson()));
    }

    public void complete(Long id, String resultJson) {
        ReportJob job = jobRepository.findById(id);
        job.setStatus(ReportStatus.DONE);
        job.setResultJson(resultJson);
        job.setCompletedAt(now());
    }

    public void fail(Long id, String message) {
        ReportJob job = jobRepository.findById(id);
        if (job == null) {
            return;
        }
        job.setStatus(ReportStatus.FAILED);
        job.setErrorMessage(message == null ? "Unknown error" : (message.length() > 500 ? message.substring(0, 500) : message));
        job.setCompletedAt(now());
    }

    public int failInterrupted() {
        int n = 0;
        for (ReportStatus s : new ReportStatus[]{ReportStatus.QUEUED, ReportStatus.RUNNING}) {
            for (ReportJob j : jobRepository.findByStatus(s)) {
                j.setStatus(ReportStatus.FAILED);
                j.setErrorMessage("Interrupted by a server restart");
                j.setCompletedAt(now());
                n++;
            }
        }
        return n;
    }

    // ---------------------------------------------------------------- helpers

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private int hoursOf(String paramsJson) {
        try {
            String digits = paramsJson.replaceAll("[^0-9]", "");
            return Integer.parseInt(digits);
        } catch (RuntimeException e) {
            return DEFAULT_HOURS;
        }
    }

    private ReportJobView view(ReportJob j, boolean withResult) {
        return new ReportJobView(j.getId(), j.getReportType().name(), j.getStatus().name(), j.getParamsJson(),
                j.getErrorMessage(),
                j.getRequestedAt() == null ? null : j.getRequestedAt().toString(),
                j.getCompletedAt() == null ? null : j.getCompletedAt().toString(),
                withResult ? j.getResultJson() : null);
    }
}
