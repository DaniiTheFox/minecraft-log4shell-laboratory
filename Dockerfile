FROM debian:buster-slim
MAINTAINER DaniiKwiiDz ""

WORKDIR /tmp

RUN apt-get update -y && apt-get upgrade -y
RUN apt-get install nodejs -y
RUN apt-get install mariadb-server -y
RUN apt-get install build-essential -y
RUN apt-get install wget -y
RUN apt-get install git -y

# --- I N S T A L L   T H E   V U L N E R A B L E   S E R V I C E   ---

# Set environment variables to avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Update package list and install necessary dependencies (curl, wget, unzip, java, etc.)
RUN apt-get update && apt-get install -y \
    bash \
    curl \
    wget \
    unzip \
    openjdk-11-jre-headless \
    && rm -rf /var/lib/apt/lists/*

# Create the minecraft directory
RUN mkdir -p /minecraft

# Download the Minecraft Forge installer (for example, 1.17.1)
RUN wget https://files.minecraftforge.net/maven/net/minecraftforge/forge/1.17.1-37.0.78/forge-1.17.1-37.0.78-installer.jar -O /minecraft/forge-installer.jar

# Run the installer to download and setup the Minecraft server with Forge
RUN java -jar /minecraft/forge-installer.jar --installServer

# Create a start script that sets up the environment for exploiting Log4Shell vulnerability
RUN echo 'echo "Starting Minecraft Server..."' > /minecraft/start.sh && \
    echo 'java -Xmx1G -Xms1G -jar /minecraft/forge-1.17.1-37.0.78.jar nogui' >> /minecraft/start.sh && \
    chmod +x /minecraft/start.sh

# Expose Minecraft's default port (25565)
EXPOSE 25565

# Set the working directory to Minecraft
WORKDIR /minecraft

# Start the Minecraft server when the container runs
CMD ["./start.sh"]
# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

