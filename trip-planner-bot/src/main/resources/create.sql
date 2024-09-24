CREATE TABLE IF NOT EXISTS app_users (
    user_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    telegram_id INTEGER NOT NULL,
    score INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS trips (
    trip_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INTEGER REFERENCES app_users (user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    name_trip VARCHAR(100) NOT NULL,
    progress VARCHAR(10) NOT NULL -- progress - Finish/Start/Planned
);

CREATE TABLE IF NOT EXISTS points (
    point_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trip_id INTEGER REFERENCES trips (trip_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    name_point VARCHAR(100) NOT NULL,
    point_date TIMESTAMP WITH TIME ZONE NOT NULL,
    transport VARCHAR(15) NOT NULL, -- transport - Plane/Ship/Bus/Car/Train/Rollercoaster/Helicopter/Walk
    notes TEXT
);
