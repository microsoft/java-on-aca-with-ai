DROP TABLE IF EXISTS weather;

CREATE TABLE weather (
   city         VARCHAR(255) NOT NULL UNIQUE PRIMARY KEY,
   description  VARCHAR(255) NOT NULL,
   icon         VARCHAR(255) NOT NULL
);

INSERT INTO weather (city, description, icon) VALUES ('Paris, France', 'Very cloudy!', 'weather-fog');
INSERT INTO weather (city, description, icon) VALUES ('London, UK', 'Quite cloudy', 'weather-pouring');
