package lk.jiat.smartcity360.arquillian;

import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

final class Deployments {

    private Deployments() {
    }

    static WebArchive war(String name, String... packages) {
        WebArchive war = ShrinkWrap.create(WebArchive.class, name + ".war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        for (String pkg : packages) {
            war.addPackages(true, Filters.exclude(".*(Test|IT)(\\$.*)?\\.class"), pkg);
        }
        return war;
    }
}
