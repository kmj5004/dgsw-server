package com.kmj5004.hdljudge.judge;

import com.kmj5004.hdljudge.common.enums.SubmissionStatus;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.judge.adapter.SimulationOutcome;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;





@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeOrchestrator {

    private final JudgeProperties props;
    private final JudgePipeline pipeline;

    private ThreadPoolExecutor executor;

    @PostConstruct
    void init() {
        ThreadFactory tf = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "judge-worker-" + counter.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        };
        int pool = Math.max(1, props.workerPoolSize());
        int queue = Math.max(1, props.queueCapacity());
        this.executor = new ThreadPoolExecutor(
            pool, pool,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(queue),
            tf,
            (r, exec) -> { throw new RejectedExecutionException("judge queue full"); }
        );
        log.info("JudgeOrchestrator initialized: pool={} queue={}", pool, queue);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void enqueue(Long submissionId) {
        try {
            executor.submit(() -> processSubmission(submissionId));
        } catch (RejectedExecutionException e) {
            throw new ApiException(ErrorCode.JUDGE_QUEUE_FULL);
        }
    }

    public Stats stats() {
        return new Stats(
            executor.getQueue().size(),
            ((LinkedBlockingQueue<Runnable>) executor.getQueue()).remainingCapacity()
                + executor.getQueue().size(),
            executor.getCorePoolSize(),
            executor.getPoolSize(),
            executor.getActiveCount(),
            executor.getCompletedTaskCount()
        );
    }

    public record Stats(
        int queueDepth,
        int queueCapacity,
        int workerPoolSize,
        int liveWorkerThreads,
        int activeWorkers,
        long completedJobs
    ) {}

    private void processSubmission(Long submissionId) {
        JobContext ctx;
        try {
            ctx = pipeline.claim(submissionId);
        } catch (Exception e) {
            log.error("Failed to claim submission {}", submissionId, e);
            try {
                pipeline.markFailed(submissionId, SubmissionStatus.FAILED);
            } catch (Exception ignored) {

            }
            return;
        }

        SimulationOutcome outcome;
        try {
            outcome = pipeline.execute(ctx);
        } catch (Exception e) {
            log.error("Simulation execution failed for submission {}", submissionId, e);
            try {
                pipeline.markFailed(submissionId, SubmissionStatus.FAILED);
            } catch (Exception ignored) {

            }
            return;
        }

        try {
            pipeline.score(ctx, outcome);
        } catch (Exception e) {
            log.error("Failed to score submission {}", submissionId, e);
            try {
                pipeline.markFailed(submissionId, SubmissionStatus.FAILED);
            } catch (Exception ignored) {

            }
        }
    }
}
