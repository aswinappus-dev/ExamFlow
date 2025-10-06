 DROP TABLE IF EXISTS exam_schedule;

-- Create the final table without the 'exam_name' column
CREATE TABLE exam_schedule (
    id INT AUTO_INCREMENT PRIMARY KEY,
    slot_start_time DATETIME NOT NULL,
    slot_end_time DATETIME NOT NULL,
    randomization_confirmed BOOLEAN NOT NULL DEFAULT FALSE
);

