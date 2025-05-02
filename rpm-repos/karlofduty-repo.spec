%global debug_package %{nil}

Name:       karlofduty-repo
Summary:    Installs the KarlofDuty.com package repository
Version:    1.0.1
Release:    1%{?dist}
BuildArch:  x86_64
License:    GPLv3
URL:        https://github.com/KarlOfDuty/CIUtilities
Packager:   KarlofDuty
Source:     rpm-source.tar.gz

%description
Adds repo files and gpg keys for the packages on repo.karlofduty.com

%prep
%setup -q

%install
if [[ -d %{_rpmdir}/%{_arch} ]]; then
  %{__rm} %{_rpmdir}/%{_arch}/*
fi

%{__install} -d %{buildroot}/etc/yum.repos.d/
%{__install} rpm-repos/karlofduty-%{distro}.repo %{buildroot}/etc/yum.repos.d/karlofduty.repo

%{__install} -d %{buildroot}/etc/pki/rpm-gpg/
%{__install} rpm-repos/RPM-GPG-KEY-karlofduty %{buildroot}/etc/pki/rpm-gpg/

%files
%config %attr(0644,root,root) /etc/yum.repos.d/karlofduty.repo
%config %attr(0644,root,root) /etc/pki/rpm-gpg/RPM-GPG-KEY-karlofduty