package lk.jiat.smartcity360.rest;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Vetoed;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@Provider
public class RoleAccessFeature implements DynamicFeature {

    static final boolean SECURE_BY_DEFAULT = true;

    @Override
    public void configure(ResourceInfo info, FeatureContext context) {
        Method m = info.getResourceMethod();
        Class<?> c = info.getResourceClass();

        if (m.isAnnotationPresent(Public.class)) {
            return;
        }
        Roles onMethod = m.getAnnotation(Roles.class);
        if (onMethod != null) {
            context.register(new AccessFilter(Set.of(onMethod.value()), false));
            return;
        }
        if (onClass(c, m, Public.class) != null) {
            return;
        }
        Roles onClass = onClass(c, m, Roles.class);
        if (onClass != null) {
            context.register(new AccessFilter(Set.of(onClass.value()), false));
            return;
        }
        if (SECURE_BY_DEFAULT) {
            context.register(new AccessFilter(Set.of(), true));   // any logged-in user
        }
    }

    private static <A extends Annotation> A onClass(Class<?> resourceClass, Method m, Class<A> type) {
        for (Class<?> k = resourceClass; k != null && k != Object.class; k = k.getSuperclass()) {
            A a = k.getAnnotation(type);
            if (a != null) {
                return a;
            }
        }
        return m.getDeclaringClass().getAnnotation(type);
    }

    @Vetoed
    @Priority(Priorities.AUTHORIZATION)
    private static final class AccessFilter implements ContainerRequestFilter {
        private final Set<String> roles;
        private final boolean anyLoggedIn;

        AccessFilter(Set<String> roles, boolean anyLoggedIn) {
            this.roles = roles;
            this.anyLoggedIn = anyLoggedIn;
        }

        @Override
        public void filter(ContainerRequestContext ctx) {
            if (!(ctx.getSecurityContext().getUserPrincipal() instanceof AuthenticatedUser user)) {
                ctx.abortWith(reply(401, "unauthenticated", "Please log in."));
                return;
            }
            if (anyLoggedIn) {
                return;
            }
            for (String r : roles) {
                if (user.hasRole(r)) {
                    return;
                }
            }
            ctx.abortWith(reply(403, "forbidden", "Your role is not allowed to do this."));
        }

        private static Response reply(int status, String code, String message) {
            return Response.status(status).type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", code, "message", message)).build();
        }
    }
}
