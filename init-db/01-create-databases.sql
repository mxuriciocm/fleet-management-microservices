-- Create databases for all microservices
CREATE DATABASE IF NOT EXISTS iam_db;
CREATE DATABASE IF NOT EXISTS profiles_db;
CREATE DATABASE IF NOT EXISTS vehicles_db;
CREATE DATABASE IF NOT EXISTS shipments_db;
CREATE DATABASE IF NOT EXISTS issues_db;

-- Grant permissions
GRANT ALL PRIVILEGES ON iam_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON profiles_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON vehicles_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON shipments_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON issues_db.* TO 'root'@'%';

FLUSH PRIVILEGES;
