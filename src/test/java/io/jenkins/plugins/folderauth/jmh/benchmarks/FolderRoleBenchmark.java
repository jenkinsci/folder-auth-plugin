package io.jenkins.plugins.folderauth.jmh.benchmarks;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.google.common.collect.ImmutableSet;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.plugins.folderauth.FolderBasedAuthorizationStrategy;
import io.jenkins.plugins.folderauth.roles.FolderRole;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.benchmark.jmh.JmhBenchmark;
import jenkins.benchmark.jmh.JmhBenchmarkState;
import jenkins.model.Jenkins;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static io.jenkins.plugins.folderauth.misc.PermissionWrapper.wrapPermissions;

/**
 * Benchmarks for {@link FolderRole}s based on the configuration of
 * https://github.com/jenkinsci/role-strategy-plugin/blob/master/src/test/java/jmh/benchmarks/FolderAccessBenchmark.java
 */
@JmhBenchmark
public class FolderRoleBenchmark {
    public static class MyState extends JmhBenchmarkState {

        @Override
        public void setup() throws Exception {
            Jenkins jenkins = getJenkins();
            jenkins.setSecurityRealm(new JenkinsRule().createDummySecurityRealm());

            Set<GlobalRole> globalRoles = ImmutableSet.of((
                    new GlobalRole("ADMIN", wrapPermissions(Jenkins.ADMINISTER), Collections.singleton("admin"))),
                new GlobalRole("read", wrapPermissions(Jenkins.READ), Collections.singleton("authenticated"))
            );

            Set<FolderRole> folderRoles = new HashSet<>();

            Random random = new Random(100L);

            // create the folders
            for (int i = 0; i < 10; i++) {
                String topFolderName = "TopFolder" + i;
                Folder folder = jenkins.createProject(Folder.class, topFolderName);

                Set<String> users = new HashSet<>();
                for (int k = 0; k < random.nextInt(5); k++) {
                    users.add("user" + random.nextInt(100));
                }

                FolderRole userRole = new FolderRole(topFolderName, wrapPermissions(Item.READ, Item.DISCOVER),
                    Collections.singleton(topFolderName), users);

                folderRoles.add(userRole);

                for (int j = 0; j < 5; j++) {
                    Folder bottom = folder.createProject(Folder.class, "BottomFolder" + j);

                    Set<String> maintainers = new HashSet<>(2);
                    maintainers.add("user" + random.nextInt(100));
                    maintainers.add("user" + random.nextInt(100));

                    FolderRole maintainerRole = new FolderRole(bottom.getFullName(),
                        wrapPermissions(Item.READ, Item.DISCOVER, Item.CREATE),
                        Collections.singleton(topFolderName), maintainers);

                    Set<String> admin = Collections.singleton("user" + random.nextInt(100));

                    FolderRole folderAdminRole = new FolderRole(bottom.getFullName(), wrapPermissions(Item.READ, Item.DISCOVER,
                        Item.CONFIGURE, Item.CREATE), Collections.singleton(topFolderName), admin);
                    folderRoles.add(maintainerRole);
                    folderRoles.add(folderAdminRole);

                    for (int k = 0; k < 5; k++) {
                        bottom.createProject(FreeStyleProject.class, "Project" + k);
                    }
                }
            }

            jenkins.setAuthorizationStrategy(new FolderBasedAuthorizationStrategy(globalRoles, folderRoles));
        }
    }

    @State(Scope.Thread)
    public static class ThreadState {
        @Setup(Level.Iteration)
        public void setup() {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(Objects.requireNonNull(User.getById("user33", true)).impersonate());
        }
    }

    @Benchmark
    public void renderViewSimulation(MyState state, ThreadState threadState, Blackhole blackhole) {
        blackhole.consume(state.getJenkins().getAllItems());
    }
}
