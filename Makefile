


target/jping.jar: FORCE /QOpenSys/pkgs/bin/mvn
	JAVA_HOME=/QOpenSys/QIBM/ProdData/JavaVM/jdk80/64bit /QOpenSys/pkgs/bin/mvn package
	cp target/*-with-dependencies.jar target/jping.jar

FORCE:

all: target/jping.jar

uninstall: clean
	rm -r ${INSTALL_ROOT}/QOpenSys/pkgs/lib/jping ${INSTALL_ROOT}/QOpenSys/pkgs/bin/jping ${INSTALL_ROOT}/QOpenSys/pkgs/bin/jping

clean:
	rm -r target

/QOpenSys/pkgs/bin/mvn:
	/QOpenSys/pkgs/bin/yum install maven

install: scripts/jping target/jping.jar 
	install -m 755 -o qsys -D -d ${INSTALL_ROOT}/QOpenSys/pkgs/bin ${INSTALL_ROOT}/QOpenSys/pkgs/lib/jping
	install -m 555 -o qsys scripts/jping.ibmi ${INSTALL_ROOT}/QOpenSys/pkgs/bin/jping
	install -m 444 -o qsys target/jping.jar ${INSTALL_ROOT}/QOpenSys/pkgs/lib/jping/jping.jar
