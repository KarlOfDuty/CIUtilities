%global debug_package %{nil}
%global repo_root %{_topdir}/..

Name:       karlofduty-repo
Summary:    A support ticket Discord bot
Version:    1.0.0~%(date "+%%Y%%m%%d%%H%%M%%S")git%(git rev-parse --short HEAD)
Release:    1%{?dist}
License:    GPLv3
URL:        https://github.com/KarlOfDuty/CIUtilities
Packager:   KarlofDuty

%description
Adds repo files and gpg keys for the packages on repo.karlofduty.com

%install
if [[ -d %{_rpmdir}/%{_arch} ]]; then
  %{__rm} %{_rpmdir}/%{_arch}/*
fi

%{__install} -d %{buildroot}/etc/yum.repos.d/
%{__install} %{_builddir}/rpm-repos/karlofduty-%{distro}.repo %{buildroot}/etc/yum.repos.d/karlofduty.repo

%{__install} -d %{buildroot}/etc/pki/rpm-gpg/
%{__install} %{repo_root}/rpm-repos/RPM-GPG-KEY-karlofduty %{buildroot}/etc/pki/rpm-gpg/

%files
%config %attr(0644,root,root) /etc/yum.repos.d/karlofduty.repo
%config %attr(0644,root,root) /etc/pki/rpm-gpg/RPM-GPG-KEY-karlofduty