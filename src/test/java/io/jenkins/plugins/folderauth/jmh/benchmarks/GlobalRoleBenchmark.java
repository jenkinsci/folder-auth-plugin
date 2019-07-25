package io.jenkins.plugins.folderauth.jmh.benchmarks;

import com.google.common.collect.ImmutableSet;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.plugins.folderauth.FolderBasedAuthorizationStrategy;
import io.jenkins.plugins.folderauth.acls.GlobalAclImpl;
import io.jenkins.plugins.folderauth.roles.GlobalRole;
import jenkins.benchmark.jmh.JmhBenchmark;
import jenkins.benchmark.jmh.JmhBenchmarkState;
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
import java.util.Set;

import static io.jenkins.plugins.folderauth.misc.PermissionWrapper.wrapPermissions;
import static org.junit.Assert.assertFalse;

/**
 * This benchmark is created to test the performance of GlobalRoles. This test is inspired from
 * https://github.com/jenkinsci/role-strategy-plugin/blob/master/src/test/java/jmh/benchmarks/RoleMapBenchmark.java .
 * <p>
 * This tests the scalability of the performance of {@link GlobalAclImpl} with increased number of roles.
 * Do note that "user3" does not have the {@link Item#CREATE} permission.
 */
@JmhBenchmark
@SuppressWarnings("unused")
public class GlobalRoleBenchmark {
    public static class GlobalRoles050 extends GlobalRoleBenchmarkState {
        @Override
        int getRoleCount() {
            return 50;
        }
    }

    public static class GlobalRoles100 extends GlobalRoleBenchmarkState {
        @Override
        int getRoleCount() {
            return 100;
        }
    }

    public static class GlobalRoles200 extends GlobalRoleBenchmarkState {
        @Override
        int getRoleCount() {
            return 200;
        }
    }

    public static class GlobalRoles500 extends GlobalRoleBenchmarkState {
        @Override
        int getRoleCount() {
            return 500;
        }
    }

    @State(Scope.Thread)
    public static class ThreadState {
        @Setup(Level.Iteration)
        public void setup() {
            SecurityContext holder = SecurityContextHolder.getContext();
            holder.setAuthentication(Objects.requireNonNull(User.getById("user3", true)).impersonate());
        }
    }

    @Benchmark
    public void benchmark050(GlobalRoles050 state, ThreadState threadState, Blackhole blackhole) {
        assertFalse(state.acl.hasPermission(Item.CREATE));
    }

    @Benchmark
    public void benchmark100(GlobalRoles100 state, ThreadState threadState, Blackhole blackhole) {
        blackhole.consume(state.acl.hasPermission(Item.CREATE));
    }

    @Benchmark
    public void benchmark200(GlobalRoles200 state, ThreadState threadState, Blackhole blackhole) {
        blackhole.consume(state.acl.hasPermission(Item.CREATE));
    }

    @Benchmark
    public void benchmark500(GlobalRoles500 state, ThreadState threadState, Blackhole blackhole) {
        blackhole.consume(state.acl.hasPermission(Item.CREATE));
    }
}

abstract class GlobalRoleBenchmarkState extends JmhBenchmarkState {

    GlobalAclImpl acl;

    @Override
    public void setup() {
        getJenkins().setSecurityRealm(new JenkinsRule().createDummySecurityRealm());
        Set<GlobalRole> globalRoles = new HashSet<>();
        for (int i = 0; i < getRoleCount(); i++) {
            globalRoles.add(new GlobalRole("role" + i, wrapPermissions(Item.DISCOVER, Item.CONFIGURE),
                ImmutableSet.of("user" + i)));
        }

        FolderBasedAuthorizationStrategy strategy = new FolderBasedAuthorizationStrategy(
            globalRoles, Collections.emptySet(), Collections.emptySet());
        acl = strategy.getRootACL();
        assertFalse(acl.hasPermission(Objects.requireNonNull(User.getById("user3", true)).impersonate(),
            Item.CREATE));
    }

    abstract int getRoleCount();
}
