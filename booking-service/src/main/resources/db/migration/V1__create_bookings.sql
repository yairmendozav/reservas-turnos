CREATE TABLE bookings (
                          id UUID PRIMARY KEY,
                          customer_name VARCHAR(120) NOT NULL,
                          customer_email VARCHAR(254) NOT NULL,
                          professional_id UUID NOT NULL,
                          service_id UUID NOT NULL,
                          starts_at TIMESTAMPTZ NOT NULL,
                          ends_at TIMESTAMPTZ NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_bookings_time
                              CHECK (ends_at > starts_at),

                          CONSTRAINT chk_bookings_status
                              CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);