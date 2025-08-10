package com.gardrops.shared.logging

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator

class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()
        return Runnable {
            val prev = MDC.getCopyOfContextMap()
            try {
                if (contextMap != null) MDC.setContextMap(contextMap) else MDC.clear()
                runnable.run()
            } finally {
                if (prev != null) MDC.setContextMap(prev) else MDC.clear()
            }
        }
    }
}
