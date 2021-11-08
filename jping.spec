%undefine _disable_source_fetch
Name: jping
Version: 0.0.1
Release: 0
License: Apache-2.0
Summary: Pure Java implementation of ping
Url: https://github.com/ThePrez/jping

BuildRequires: maven
BuildRequires: openjdk-11
BuildRequires: coreutils-gnu
BuildRequires: make-gnu
BuildRequires: p11-kit-trust
Requires: bash

Source0: https://github.com/ThePrez/jping/archive/refs/tags/v%{version}.tar.gz

%description
Pure Java implementation of ping
%prep
%setup

%build
gmake all


%install
INSTALL_ROOT=%{buildroot} gmake -e install

%files
%defattr(-, qsys, *none)

%{_bindir}/jping
%{_libdir}/%{name}

%changelog
* Mon Nov 08 2021 Jesse Gorzinski <jgorzins@us.ibm.com> - 0.0.1
- initial RPM release
