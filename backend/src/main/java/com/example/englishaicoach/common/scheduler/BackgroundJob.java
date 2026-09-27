package com.example.englishaicoach.common.scheduler;

import java.time.Instant;

/**
 * Port cho một worker nền. Worker tự xác định công việc đến hạn và bảo đảm
 * thao tác lặp an toàn; scheduler chỉ truyền thời điểm từ Clock chung.
 */
public interface BackgroundJob {

    BackgroundJobType type();

    void run(Instant now);
}
