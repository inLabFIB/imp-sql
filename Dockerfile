FROM mcr.microsoft.com/mssql/server:2019-latest

# BASED in repositoris:
# - https://github.com/microsoft/mssql-docker/tree/master/linux/preview/examples/mssql-customize
# Other info: https://github.com/microsoft/mssql-docker/issues/2

# Switch to root user for access to apt-get install
USER root

# Install dos2unix
RUN apt-get -y update && apt-get install -y \
    dos2unix  \
    && rm -rf /var/lib/apt/lists/*

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY scripts /usr/src/app/scripts
COPY sql_setups /usr/src/app/sql_setups

# Set LF endline in shell scripts
RUN dos2unix /usr/src/app/scripts/*.sh

# Grant permissions for to our scripts to be executable
RUN chmod +x /usr/src/app/scripts/*.sh

# Switch back to mssql user and run the entrypoint script
USER mssql
ENTRYPOINT ["/usr/src/app/scripts/entrypoint.sh"]

# Tail the setup logs to trap the process
CMD ["tail -f /dev/null"]
