-- Add test books
INSERT INTO books (id, title, author, isbn, price, description, cover_image)
VALUES (1, 'Sample Book 1', 'Author A', '978-1-23-456789-7', 19.99, 'This is a sample book description.', 'http://example.com/cover1.jpg');
-- Add test users
INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (2, 'user@i.ua', 'qwerty123', 'John', 'Smith', 'Ukraine');
-- Add test shopping carts
INSERT INTO shopping_carts (user_id) VALUES (2);
-- Add test cart item
INSERT INTO cart_items (shopping_cart_id, book_id, quantity) VALUES (2, 1, 6);
