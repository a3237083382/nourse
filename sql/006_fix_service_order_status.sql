USE `ry-vue`;

ALTER TABLE service_order
  DROP CHECK chk_service_order_status;

ALTER TABLE service_order
  ADD CONSTRAINT chk_service_order_status
  CHECK (status IN ('WAIT_START', 'SERVING', 'COMPLETED', 'CANCELED'));
