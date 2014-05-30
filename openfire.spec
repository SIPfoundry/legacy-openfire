Summary: Openfire XMPP Server
Name: openfire
Version: %{OPENFIRE_VERSION}
Release: %{?buildno:%buildno}%{!?buildno:103}
BuildRoot: %{_builddir}/%{name}-root
Source0: %{OPENFIRE_SOURCE}
Group: Applications/Communications
Vendor: Jive Software
Packager: Jive Software
License: Apache license v2.0
AutoReqProv: no
URL: http://www.igniterealtime.org/

BuildRequires: ant
BuildRequires: java-devel >= 1.7

Requires: chkconfig

# disable JAR repacking. perm denied issues on in 'clean', just a general bad idea IMHO
%define __jar_repack %{nil}

%define prefix /opt
%define homedir %{prefix}/openfire

%description
Openfire is a leading Open Source, cross-platform IM server based on the
XMPP (Jabber) protocol. It has great performance, is easy to setup and use,
and delivers an innovative feature set.

%prep
%setup -q -n openfire-%{version}

%build
cd build
ant openfire
ant -Dplugin=search plugin
cd ..

%install
# Prep the install location.
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{prefix}
# Copy over the main install tree.
cp -R target/openfire $RPM_BUILD_ROOT%{homedir}
# Set up the init script.
mkdir -p $RPM_BUILD_ROOT/etc/init.d
cp $RPM_BUILD_ROOT%{homedir}/bin/extra/redhat/openfire $RPM_BUILD_ROOT/etc/init.d/openfire
chmod 755 $RPM_BUILD_ROOT/etc/init.d/openfire
# Make the startup script executable.
chmod 755 $RPM_BUILD_ROOT%{homedir}/bin/openfire.sh
# Set up the sysconfig file.
mkdir -p $RPM_BUILD_ROOT/etc/sysconfig
cp $RPM_BUILD_ROOT%{homedir}/bin/extra/redhat/openfire-sysconfig $RPM_BUILD_ROOT/etc/sysconfig/openfire
# Copy over the documentation
cp -R documentation $RPM_BUILD_ROOT%{homedir}/documentation
# Copy over the i18n files
cp -R src/i18n $RPM_BUILD_ROOT%{homedir}/resources/i18n
# Make sure scripts are executable
chmod 755 $RPM_BUILD_ROOT%{homedir}/bin/extra/openfired
chmod 755 $RPM_BUILD_ROOT%{homedir}/bin/extra/redhat-postinstall.sh
# Move over the embedded db viewer pieces
mv $RPM_BUILD_ROOT%{homedir}/bin/extra/embedded-db.rc $RPM_BUILD_ROOT%{homedir}/bin
mv $RPM_BUILD_ROOT%{homedir}/bin/extra/embedded-db-viewer.sh $RPM_BUILD_ROOT%{homedir}/bin
# We don't really need any of these things.
rm -rf $RPM_BUILD_ROOT%{homedir}/bin/extra
rm -f $RPM_BUILD_ROOT%{homedir}/bin/*.bat
rm -rf $RPM_BUILD_ROOT%{homedir}/resources/nativeAuth/osx-ppc
rm -rf $RPM_BUILD_ROOT%{homedir}/resources/nativeAuth/solaris-sparc
rm -rf $RPM_BUILD_ROOT%{homedir}/resources/nativeAuth/win32-x86
rm -f $RPM_BUILD_ROOT%{homedir}/lib/*.dll

%clean
rm -rf $RPM_BUILD_ROOT || true

%files
%defattr(-,daemon,daemon)
%attr(755, daemon, daemon) %dir %{homedir}
%dir %{homedir}/bin
%{homedir}/bin/openfire.sh
%{homedir}/bin/openfirectl
%config(noreplace) %{homedir}/bin/embedded-db.rc
%{homedir}/bin/embedded-db-viewer.sh
%dir %{homedir}/conf
%config(noreplace) %{homedir}/conf/openfire.xml
%config(noreplace) %{homedir}/conf/openfire.properties
%config(noreplace) %{homedir}/conf/security.xml
%config(noreplace) %{homedir}/conf/crowd.properties
%dir %{homedir}/lib
%{homedir}/lib/*.jar
%{homedir}/lib/log4j.xml
%dir %{homedir}/logs
%dir %{homedir}/plugins
%{homedir}/plugins/search.jar
%dir %{homedir}/plugins/admin
%{homedir}/plugins/admin/*
%dir %{homedir}/resources
%dir %{homedir}/resources/database
%{homedir}/resources/database/*.sql
%dir %{homedir}/resources/database/upgrade
%dir %{homedir}/resources/database/upgrade/*
%{homedir}/resources/database/upgrade/*/*
%dir %{homedir}/resources/i18n
%{homedir}/resources/i18n/*
%dir %{homedir}/resources/nativeAuth
%dir %{homedir}/resources/nativeAuth/linux-i386
%{homedir}/resources/nativeAuth/linux-i386/*
%attr(755, daemon, daemon) %dir %{homedir}/resources/security
%dir %{homedir}/resources/spank
%{homedir}/resources/spank/index.html
%dir %{homedir}/resources/spank/WEB-INF
%{homedir}/resources/spank/WEB-INF/web.xml
%config(noreplace) %{homedir}/resources/security/keystore
%config(noreplace) %{homedir}/resources/security/truststore
%config(noreplace) %{homedir}/resources/security/client.truststore
%doc %{homedir}/documentation
%{_sysconfdir}/init.d/openfire
%config(noreplace) %{_sysconfdir}/sysconfig/openfire

%changelog
* %{OPENFIRE_BUILDDATE} Jive Software <webmaster@jivesoftware.com> %{OPENFIRE_VERSION}-1
- Automatic RPM build.
