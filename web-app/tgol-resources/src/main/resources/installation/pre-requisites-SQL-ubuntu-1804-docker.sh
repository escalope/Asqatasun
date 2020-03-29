#!/bin/bash

# MAINTAINER Matthieu Faure <mfaure@asqatasun.org>

set -o errexit

if [[ $UID -ne 0 ]]; then
    echo "$0 must be run as root"
    exit 1
fi

#############################################
# Variables
#
# --> adjust to your own configuration
#
#############################################

# Mysql
MYSQL_ROOT_PASSWD=mysqlRootPassword
MYSQL_CONF_DIR=/etc/mysql/conf.d
MYSQL_CONF_FILE_FOR_ASQATASUN=asqatasun.cnf

# Mysql for Asqatasun
DATABASE_USER=asqatasun
DATABASE_PASSWD=asqaP4sswd
DATABASE_DBNAME=asqatasun
DATABASE_HOST=localhost

#
# That's OK, please do not modify things further,
# you can run this script (as root) and proceed with the install.sh
#
# -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*

SYSTEMCTL=/bin/systemctl

#############################################
# init
#############################################

fail() {
    echo ""
	echo "FAILURE : $*"
    echo ""
	exit 1
}

#############################################
# Mysql config
#############################################

cat >${MYSQL_CONF_DIR}/${MYSQL_CONF_FILE_FOR_ASQATASUN} <<EOF
[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4

[mysqld]
collation-server = utf8mb4_general_ci
init-connect='SET NAMES utf8mb4'
character-set-server = utf8mb4
max_allowed_packet = 64M
innodb_file_per_table = 1
innodb_log_file_size = 256M
EOF

# Workaround for bug related to packages of MariaDB/Mysql on Debian/Ubuntu plateforms
# See https://github.com/Asqatasun/Asqatasun/issues/311
echo '!include /etc/mysql/conf.d/asqatasun.cnf' >>/etc/mysql/my.cnf

#"${SYSTEMCTL}" restart mariadb.service

# Create Asqatasun database
# Note: \` are mandatory to ensure database name is protected (thus allowing names containing hyphens)
mysql \
    -u root \
    --password="${MYSQL_ROOT_PASSWD}" \
    --execute="GRANT USAGE ON * . * TO '${DATABASE_USER}'@'${DATABASE_HOST}' IDENTIFIED BY '${DATABASE_PASSWD}'; \
        CREATE DATABASE IF NOT EXISTS \`${DATABASE_DBNAME}\` CHARACTER SET utf8mb4; \
        GRANT ALL PRIVILEGES ON \`${DATABASE_DBNAME}\` . * TO '${DATABASE_USER}'@'${DATABASE_HOST}'; \
        FLUSH PRIVILEGES;"
