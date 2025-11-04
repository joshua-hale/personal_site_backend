
'EOF'
#!/bin/bash

export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://personal-site-db.czcmkaeiuoaz.eu-west-2.rds.amazonaws.com:5432/appdb
export DATABASE_USERNAME=appuser
export DATABASE_PASSWORD=Westbrom!17
export MAIL_USERNAME=joshuahale173@gmail.com
export MAIL_PASSWORD=brghqmykrtsqmljm
export CONTACT_RECIPIENT_EMAIL=joshuahale173@gmail.com
export CONTACT_FROM_EMAIL=joshuahale173@gmail.com
export CORS_ALLOWED_ORIGINS=http://localhost:5173
export SPRING_FLYWAY_ENABLED=false

echo "Starting JAR with prod profile..."
java -jar app.jar
