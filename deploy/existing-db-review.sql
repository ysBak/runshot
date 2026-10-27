-- 읽기 전용 사전 점검. 사용자 정보는 외부에 공유하지 마세요.
SHOW CREATE TABLE users;
SHOW CREATE TABLE running_records;
SELECT id, running_date, distance FROM running_records WHERE user_id IS NULL;
SELECT r.id, r.user_id FROM running_records r LEFT JOIN users u ON r.user_id = u.id
WHERE r.user_id IS NOT NULL AND u.id IS NULL;
-- 이전 create() 버그로 소유자가 NULL인 기록이 있을 수 있습니다.
-- 실제 소유자를 확인하기 전에는 임의로 사용자를 연결하거나 삭제하지 않습니다.
-- DB 백업 및 소유자 복구 후, 현재 스키마와 차이가 있을 때만 수동 실행:
-- ALTER TABLE running_records MODIFY content VARCHAR(500);
-- ALTER TABLE running_records MODIFY user_id BIGINT NOT NULL;
-- CREATE INDEX idx_runs_user_date ON running_records(user_id, running_date, id);
