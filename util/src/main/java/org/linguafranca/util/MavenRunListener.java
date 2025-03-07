/*
 * Copyright (c) 2025. Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.linguafranca.util;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Helper class to identify the test being run by maven surefire in the log output. This
 * is particularly useful if there is a lot of "chatty" testing going on.
 * <p>
 * See: <a href="https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit.html#Using_Custom_Listeners_and_Reporters">Using Custom Listeners and Reporters</a>
 */
@RunListener.ThreadSafe
public class MavenRunListener extends RunListener {
    static Logger logger = LoggerFactory.getLogger("MavenRunListener");
    static Marker marker = MarkerFactory.getMarker("Starting Test");
    static Marker endMarker = MarkerFactory.getMarker("Finished Test");
    static Marker failMarker = MarkerFactory.getMarker("Failed Test");
    static Marker assumeFailMarker = MarkerFactory.getMarker("Assumption Failed");
    static Marker ignoredMarker = MarkerFactory.getMarker("Ignored Test");

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        logger.info(marker, "{}", description.getDisplayName());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        logger.error(failMarker, "{} {}", failure.getMessage(), failure.getDescription().getDisplayName());
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        super.testAssumptionFailure(failure);
        logger.warn(assumeFailMarker, "{} {}", failure.getMessage(), failure.getDescription().getDisplayName());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
        logger.info(ignoredMarker, "{}", description.getDisplayName());
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        logger.info(endMarker, "Finished {}", description.getDisplayName());
    }
}
