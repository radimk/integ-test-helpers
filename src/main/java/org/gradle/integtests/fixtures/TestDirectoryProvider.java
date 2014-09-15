package org.gradle.integtests.fixtures;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A JUnit rule which provides a unique temporary folder for the test.
 */
public class TestDirectoryProvider implements TestRule {
    private String determinePrefix() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().endsWith("Test") || element.getClassName().endsWith("Spec")) {
                return Iterables.getLast(Splitter.on('.').split(element.getClassName())) + "/unknown-test-" + testCounter.getAndIncrement();
            }

        }

        return "unknown-test-class-" + testCounter.getAndIncrement();
    }

    public Statement apply(final Statement base, Description description) {
        init(description.getMethodName(), description.getTestClass().getSimpleName());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
                getTestDirectory().delete();
            }

        };
    }

    private void init(String methodName, String className) {
        if (methodName == null) {
            // must be a @ClassRule use the rule's class name instead
            methodName = getClass().getSimpleName();
        }

        if (prefix == null) {
            String safeMethodName = methodName.replaceAll("\\s", "_").replace(File.pathSeparator, "_").replace(":", "_").replace("\"", "_");
            if (safeMethodName.length() > 64) {
                safeMethodName = safeMethodName.substring(0, 32) + "..." + safeMethodName.substring((int) safeMethodName.length() - 32);
            }

            prefix = String.format("%s/%s", className, safeMethodName);
        }

    }

    public File getTestDirectory() {
        if (dir == null) {
            if (prefix == null) {
                // This can happen if this is used in a constructor or a @Before method. It also happens when using
                // @RunWith(SomeRunner) when the runner does not support rules.
                prefix = determinePrefix();
            }

            for (int counter = 1; true; counter++) {
                dir = new File(root, counter == 1 ? prefix : String.format("%s%d", prefix, counter));
                if (dir.mkdirs()) {
                    break;
                }

            }

        }

        return dir;
    }

    public File file(Object... path) {
        File current = getTestDirectory().getAbsoluteFile();
        for (Object p : path) {
            current = new File(current, p.toString());
        }

        try {
            return current.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not canonicalise '%s'.", current), e);
        }

    }

    public File createFile(Object... path) {
        File file = file(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not create new file '%s'.", file), e);
        }
        return ((File) (file));
    }

    public File createDir(Object... path) {
        File dir = file(path);
        dir.mkdirs();
        return ((File) (dir));
    }

    private File dir;
    private String prefix;
    private static File root;
    private static AtomicInteger testCounter = new AtomicInteger(1);
}
