/*
 * Asqatasun - Automated webpage assessment
 * Copyright (C) 2008-2019  Asqatasun.org
 *
 * This file is part of Asqatasun.
 *
 * Asqatasun is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact us by mail: asqatasun AT asqatasun DOT org
 */
package org.asqatasun.service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.asqatasun.entity.audit.Audit;
import org.asqatasun.service.command.AuditCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author koj
 */
@Component
public class AuditServiceThreadQueueImpl implements AuditServiceThreadQueue, AuditServiceThreadListener {
    
    private static final Logger LOGGER = Logger.getLogger(AuditServiceThreadQueueImpl.class);
    
    private final Queue<AuditCommand> pageAuditWaitQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AuditCommand> scenarioAuditWaitQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AuditCommand> uploadAuditWaitQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AuditCommand> siteAuditWaitQueue = new ConcurrentLinkedQueue<>();

    private final List<AuditServiceThread> pageAuditExecutionList = new ArrayList<>();
    private final List<AuditServiceThread> scenarioAuditExecutionList = new ArrayList<>();
    private final List<AuditServiceThread> uploadAuditExecutionList = new ArrayList<>();
    private final List<AuditServiceThread> siteAuditExecutionList = new ArrayList<>();

    private Integer pageAuditExecutionListMax;
    private Integer scenarioAuditExecutionListMax;
    private Integer uploadAuditExecutionListMax;
    private Integer siteAuditExecutionListMax;
    private Set<AuditServiceListener> listeners = null;
    public Set<AuditServiceListener> getListeners() {
        return listeners;
    }
    @Autowired
    private AuditServiceThreadFactory auditServiceThreadFactory;
    public void setAuditServiceThreadFactory(AuditServiceThreadFactory auditServiceThreadFactory) {
        this.auditServiceThreadFactory = auditServiceThreadFactory;
    }

    public AuditServiceThreadQueueImpl(
        @Value("${app.engine.maxConcurrentSize.page:3}") Integer pageAuditExecutionListMax,
        @Value("${app.engine.maxConcurrentSize.scenario:3}") Integer scenarioAuditExecutionListMax,
        @Value("${app.engine.maxConcurrentSize.upload:3}") Integer uploadAuditExecutionListMax,
        @Value("${app.engine.maxConcurrentSize.site:3}") Integer siteAuditExecutionListMax) {

        this.pageAuditExecutionListMax = pageAuditExecutionListMax;
        this.scenarioAuditExecutionListMax = scenarioAuditExecutionListMax;
        this.uploadAuditExecutionListMax = uploadAuditExecutionListMax;
        this.siteAuditExecutionListMax = siteAuditExecutionListMax;
    }

    @Override
    public void add(AuditServiceListener listener) {
        if (listeners == null) {
            listeners = new HashSet<>();
        }
        listeners.add(listener);
    }

    @Override
    public void remove(AuditServiceListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
    }

    @Override
    public synchronized void addPageAudit(AuditCommand auditCommand) {
        pageAuditWaitQueue.offer(auditCommand);
        processPageAuditWaitQueue();
    }

    private synchronized void processPageAuditWaitQueue() {
        processAuditWaitQueue(
                pageAuditWaitQueue,
                pageAuditExecutionList,
                pageAuditExecutionListMax);
    }

    @Override
    public synchronized void addScenarioAudit(AuditCommand auditCommand) {
        scenarioAuditWaitQueue.offer(auditCommand);
        processScenarioAuditWaitQueue();
    }

    private synchronized void processScenarioAuditWaitQueue() {
        processAuditWaitQueue(
                scenarioAuditWaitQueue,
                scenarioAuditExecutionList,
                scenarioAuditExecutionListMax);
    }

    @Override
    public synchronized void addPageUploadAudit(AuditCommand auditCommand) {
        uploadAuditWaitQueue.offer(auditCommand);
        processPageUploadAuditWaitQueue();
    }

    private synchronized void processPageUploadAuditWaitQueue() {
        processAuditWaitQueue(
                uploadAuditWaitQueue,
                uploadAuditExecutionList,
                uploadAuditExecutionListMax);
    }

    @Override
    public synchronized void addSiteAudit(AuditCommand auditCommand) {
        siteAuditWaitQueue.offer(auditCommand);
        processSiteAuditWaitQueue();
    }

    private synchronized void processSiteAuditWaitQueue() {
        processAuditWaitQueue(
                siteAuditWaitQueue,
                siteAuditExecutionList,
                siteAuditExecutionListMax);
    }

    /**
     *
     * @param auditWaitQueue
     * @param auditExecutionList
     * @param auditExecutionListMax
     * @return
     */
    private synchronized void processAuditWaitQueue(
            Queue<AuditCommand> auditWaitQueue,
            List<AuditServiceThread> auditExecutionList,
            int auditExecutionListMax) {
        if (auditWaitQueue.peek() == null) {
            return;
        }
        if (auditExecutionList.size() < auditExecutionListMax) {
            AuditCommand auditCommand = auditWaitQueue.poll();
            LOGGER.debug("auditCommand polled");
            AuditServiceThread auditServiceThread = auditServiceThreadFactory.create(auditCommand);
            LOGGER.debug("AuditServiceThread created from auditCommand");
            auditServiceThread.add(this);
            auditExecutionList.add(auditServiceThread);
            new Thread(auditServiceThread).start();
            LOGGER.debug("AuditServiceThread started");
        } else {
            LOGGER.debug("Execution requested but max simultaneous execution reached");
        }
    }

    @Override
    public void auditCompleted(AuditServiceThread thread) {
        fireAuditCompleted(thread.getAudit());
        terminateProperlyAudit(thread);
    }

    @Override
    public void auditCrashed(AuditServiceThread thread, Exception exception) {
        fireAuditCrashed(thread.getAudit(), exception);
        terminateProperlyAudit(thread);
    }

    /**
     * Interrogate each execution List to properly remove the current thread
     * and eventually launch audit waiting in the queue
     * @param thread 
     */
    private void terminateProperlyAudit(AuditServiceThread thread){
        thread.remove(this);
        if (pageAuditExecutionList.remove(thread)) {
            processPageAuditWaitQueue();
        } else if (scenarioAuditExecutionList.remove(thread)) {
            processScenarioAuditWaitQueue();
        } else if (uploadAuditExecutionList.remove(thread)) {
            processPageUploadAuditWaitQueue();
        } else if (siteAuditExecutionList.remove(thread)) {
            processSiteAuditWaitQueue();
        }
    }

    private void fireAuditCompleted(Audit audit) {
        if (listeners == null) {
            return;
        }
        for (AuditServiceListener listener : listeners) {
            listener.auditCompleted(audit);
        }
    }

    private void fireAuditCrashed(Audit audit, Exception exception) {
        if (listeners == null) {
            return;
        }
        for (AuditServiceListener listener : listeners) {
            listener.auditCrashed(audit, exception);
        }
    }

}
