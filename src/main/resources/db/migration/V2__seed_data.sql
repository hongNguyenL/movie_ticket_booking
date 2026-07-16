-- Roles
INSERT INTO roles (id, name) VALUES
(1, 'ROLE_CUSTOMER'),
(2, 'ROLE_ADMIN');

-- Users
INSERT INTO users (id, username, email, password, full_name, enabled) VALUES
(1, 'admin', 'admin@movieticket.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Admin', TRUE),
(2, 'john_doe', 'john@example.com', '$2a$10$p4K8aPYoPJkXqBHKG6Gy4eU5hj7HhYkHJZKzZz5mJFpRFRZ5yPcO', 'John Doe', TRUE),
(3, 'jane_smith', 'jane@example.com', '$2a$10$p4K8aPYoPJkXqBHKG6Gy4eU5hj7HhYkHJZKzZz5mJFpRFRZ5yPcO', 'Jane Smith', TRUE);

-- User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 2),
(2, 1),
(3, 1);

-- Genres
INSERT INTO genres (id, name, slug, description) VALUES
(1, 'Action', 'action', 'Fast-paced films featuring physical feats, fights, and stunts'),
(2, 'Comedy', 'comedy', 'Light-hearted films designed to entertain and amuse'),
(3, 'Drama', 'drama', 'Story-driven films with emotional themes and character development'),
(4, 'Horror', 'horror', 'Films designed to frighten and invoke fear in the audience'),
(5, 'Science Fiction', 'science-fiction', 'Speculative films exploring futuristic concepts and advanced technology'),
(6, 'Romance', 'romance', 'Love stories focusing on passion, emotion, and relationships'),
(7, 'Thriller', 'thriller', 'Suspenseful films with tension, excitement, and plot twists'),
(8, 'Animation', 'animation', 'Films created through animated techniques and visual artistry'),
(9, 'Documentary', 'documentary', 'Non-fictional films documenting reality and factual events'),
(10, 'Fantasy', 'fantasy', 'Films set in imaginary worlds with magical and supernatural elements');

-- Directors
INSERT INTO directors (id, name, biography, birth_date, nationality) VALUES
(1, 'Christopher Nolan', 'British-American filmmaker known for thought-provoking narratives and stunning visuals', '1970-07-30', 'British-American'),
(2, 'Greta Gerwig', 'American actress and director celebrated for her nuanced character-driven stories', '1983-08-04', 'American'),
(3, 'Bong Joon-ho', 'South Korean director acclaimed for genre-blending films with sharp social commentary', '1969-09-14', 'South Korean');

-- Actors
INSERT INTO actors (id, name, biography, birth_date, nationality) VALUES
(1, 'Robert Downey Jr.', 'Acclaimed American actor known for his versatile roles in both blockbusters and dramas', '1965-04-04', 'American'),
(2, 'Scarlett Johansson', 'American actress recognized as one of the highest-grossing box office stars', '1984-11-22', 'American'),
(3, 'Leonardo DiCaprio', 'American actor and producer celebrated for his work in epic and dramatic films', '1974-11-11', 'American'),
(4, 'Margot Robbie', 'Australian actress and producer known for her dynamic performances', '1990-07-02', 'Australian'),
(5, 'Timothée Chalamet', 'French-American actor praised for his compelling performances in independent and mainstream films', '1995-12-27', 'French-American'),
(6, 'Zendaya', 'American actress and singer recognized for her powerful screen presence', '1996-09-01', 'American');

-- Movies
INSERT INTO movies (id, title, description, duration, release_date, language, rating, genre_id, director_id) VALUES
(1, 'The Dark Horizon', 'A thrilling sci-fi adventure where humanity faces its greatest challenge beyond the stars', 148, '2025-06-15', 'English', 8.5, 5, 1),
(2, 'Laughing Matters', 'A heartwarming comedy about an unlikely group of friends navigating lifes absurdities', 112, '2025-07-20', 'English', 7.8, 2, 2),
(3, 'Echoes of Silence', 'A powerful drama exploring love, loss, and redemption across generations', 135, '2025-05-10', 'English', 8.2, 3, 3),
(4, 'Starfall', 'An epic space opera following a crew of explorers on a perilous mission to save civilization', 162, '2025-12-18', 'English', 9.0, 5, 1),
(5, 'Midnight Shadows', 'A gripping psychological thriller where nothing is as it seems', 118, '2025-10-31', 'English', 7.5, 7, 2);

-- Movie Actors
INSERT INTO movie_actors (movie_id, actor_id, role_name) VALUES
(1, 1, 'Captain James Ryder'),
(1, 2, 'Dr. Elena Vasquez'),
(2, 4, 'Sophie Bennett'),
(2, 5, 'Marcus Lee'),
(3, 3, 'Thomas Ashford'),
(3, 6, 'Isabella Cruz'),
(4, 1, 'Commander David Stone'),
(4, 2, 'Lt. Commander Mia Chen'),
(4, 3, 'Admiral William Hayes'),
(5, 5, 'Alex Turner'),
(5, 6, 'Detective Sarah Blake');

-- Cinemas
INSERT INTO cinemas (id, name, address, city, state, phone, email, description) VALUES
(1, 'Cinema City Center', '123 Main Street, Downtown', 'New York', 'NY', '+1-212-555-0100', 'citycenter@movieticket.com', 'Premier cinema in the heart of the city with state-of-the-art screens'),
(2, 'Star Cinema Mall', '456 Oak Avenue, Westside Mall', 'Los Angeles', 'CA', '+1-310-555-0200', 'starcinema@movieticket.com', 'Modern multiplex cinema located in the Star Mall complex');

-- Halls
INSERT INTO halls (id, name, cinema_id, capacity, description) VALUES
(1, 'Hall 1', 1, 50, 'Main auditorium with Dolby Atmos sound system'),
(2, 'Hall 2', 1, 40, 'Premium hall with luxury recliner seating'),
(3, 'Hall 1', 2, 50, 'IMAX digital screen with 3D capability'),
(4, 'Hall 2', 2, 40, 'Compact hall with intimate viewing experience');

-- Seats for Hall 1 (cinema 1) - 5 rows, 10 seats each, row E is VIP
INSERT INTO seats (hall_id, row_label, seat_number, seat_type)
SELECT 1, row_label, seat_number,
       CASE WHEN row_label = 'E' THEN 'VIP' ELSE 'STANDARD' END
FROM generate_series(1, 10) AS seat_number,
     (SELECT unnest(ARRAY['A', 'B', 'C', 'D', 'E']) AS row_label) AS rows;

-- Seats for Hall 2 (cinema 1) - 5 rows, 8 seats each, row E is VIP
INSERT INTO seats (hall_id, row_label, seat_number, seat_type)
SELECT 2, row_label, seat_number,
       CASE WHEN row_label = 'E' THEN 'VIP' ELSE 'STANDARD' END
FROM generate_series(1, 8) AS seat_number,
     (SELECT unnest(ARRAY['A', 'B', 'C', 'D', 'E']) AS row_label) AS rows;

-- Seats for Hall 1 (cinema 2) - 5 rows, 10 seats each, row E is VIP
INSERT INTO seats (hall_id, row_label, seat_number, seat_type)
SELECT 3, row_label, seat_number,
       CASE WHEN row_label = 'E' THEN 'VIP' ELSE 'STANDARD' END
FROM generate_series(1, 10) AS seat_number,
     (SELECT unnest(ARRAY['A', 'B', 'C', 'D', 'E']) AS row_label) AS rows;

-- Seats for Hall 2 (cinema 2) - 5 rows, 8 seats each, row E is VIP
INSERT INTO seats (hall_id, row_label, seat_number, seat_type)
SELECT 4, row_label, seat_number,
       CASE WHEN row_label = 'E' THEN 'VIP' ELSE 'STANDARD' END
FROM generate_series(1, 8) AS seat_number,
     (SELECT unnest(ARRAY['A', 'B', 'C', 'D', 'E']) AS row_label) AS rows;

-- Showtimes: 3 per movie per hall for today and tomorrow
INSERT INTO showtimes (movie_id, hall_id, start_time, end_time, ticket_price)
SELECT
    m.id,
    h.id,
    show_date + start_time,
    show_date + end_time,
    ticket_price
FROM movies m
CROSS JOIN halls h
CROSS JOIN (VALUES
    (CURRENT_DATE, CURRENT_DATE),
    (CURRENT_DATE + 1, CURRENT_DATE + 1)
) AS dates(show_date, dummy)
CROSS JOIN (VALUES
    (TIME '10:00', TIME '12:30', 12.50),
    (TIME '14:00', TIME '16:30', 15.00),
    (TIME '19:00', TIME '21:30', 18.00)
) AS times(start_time, end_time, ticket_price);
