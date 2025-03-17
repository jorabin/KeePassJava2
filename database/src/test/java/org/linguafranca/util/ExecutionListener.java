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

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.slf4j.*;

public class ExecutionListener implements TestExecutionListener {

    static Logger logger = LoggerFactory.getLogger("Test Execution Listener");
    static Marker marker = MarkerFactory.getMarker("Starting");
    static Marker endMarker = MarkerFactory.getMarker("Finished");
    static Marker failMarker = MarkerFactory.getMarker("Failed");
    static Marker assumeFailMarker = MarkerFactory.getMarker("Assumption Failed");
    static Marker ignoredMarker = MarkerFactory.getMarker("Ignored");
    static Marker skippedMarker = MarkerFactory.getMarker("Skipped");

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        logger.info(ignoredMarker, "{} {}", testIdentifier.getDisplayName(), reason);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        logger.info(marker, "{} {}", testIdentifier.getType(), testIdentifier.getDisplayName());
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        MDC.put("result", testExecutionResult.getStatus().toString());
        switch (testExecutionResult.getStatus()) {
            case FAILED:
                logger.error(failMarker, "{}", testIdentifier.getDisplayName());
                break;
            case ABORTED:
                logger.warn(ignoredMarker, "{}", testIdentifier.getDisplayName());
                break;
            case SUCCESSFUL:
                logger.info(endMarker,"{}", testIdentifier.getDisplayName());
        }
        MDC.clear();
    }
}
