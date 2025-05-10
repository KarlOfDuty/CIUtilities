def prepare_gpg_key()
{
  withCredentials([string(credentialsId: 'JENKINS_GPG_KEY_PASSWORD', variable: 'JENKINS_GPG_KEY_PASSWORD')])
  {
    sh '/usr/lib/gnupg/gpg-preset-passphrase --passphrase "$JENKINS_GPG_KEY_PASSWORD" --preset 0D27E4CD885E9DD79C252E825F70A1590922C51E'
  }
}

def build_rpm_package(String distro, String package_name)
{
  sh "mkdir -p ${distro}/SOURCES"
  sh "git archive --format=tar.gz HEAD > '${distro}/SOURCES/rpm-source.tar.gz'"
  sh "rpmbuild -ba rpm-repos/${package_name}.spec --define \"_topdir ${WORKSPACE}/${distro}\" --define 'distro ${distro}'"
  sh "cp ${distro}/RPMS/x86_64/${package_name}-*.x86_64.rpm ${distro}/"
  sh "cp ${distro}/SRPMS/${package_name}-*.src.rpm ${distro}/"
}

// The deb packages are a bit too complicated to make a standardised build function for,
// each repo will have to use their own script.

def sign_rpm_package(String rpm_path)
{
  sh "rpmsign --define '_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>' --addsign ${rpm_path}"
  sh "rpm -vv --checksig ${rpm_path}"
}

def sign_deb_package(String deb_path, String dsc_path)
{
  sh "cat ${dsc_path} | gpg -u 2FEAAE97C813C486 --clearsign > ${dsc_path}"
  sh "gpg --verify ${dsc_path}"
  sh "/usr/bin/site_perl/debsigs --sign=origin -k 2FEAAE97C813C486 ${deb_path}"
  sh "debsig-verify ${deb_path}"
}

def publish_rpm_package(String distro_dir, String rpm_path, String srpm_path, String package_name)
{
  def repo_dir="/usr/share/nginx/repo.karlofduty.com/${distro_dir}"
  def package_dir="${repo_dir}/x86_64/Packages/${package_name}"
  def source_dir="${repo_dir}/source/Packages/${package_name}"

  sh "mkdir -p ${package_dir}"
  sh "mkdir -p ${source_dir}"

  sh "cp ${rpm_path} ${package_dir}"
  sh "cp ${rpm_path} ${source_dir}"

  sh "createrepo_c --update ${repo_dir}/x86_64"
  sh "createrepo_c --update ${repo_dir}/source"
}

def publish_deb_package(String distro, String package_name, String package_dir, String build_root, String component)
{
  def repo_dir="/usr/share/nginx/repo.karlofduty.com/${distro}"
  def pool_dir="${repo_dir}/pool/${component}/${package_dir}"
  def dists_bin_dir="${repo_dir}/dists/${distro}/${component}/binary-amd64"
  def dists_src_dir="${repo_dir}/dists/${distro}/${component}/source"

  // Copy package and sources to pool directory
  sh "mkdir -p ${pool_dir}"
  sh "cp ${build_root}/${package_name}_*_amd64.deb ${pool_dir}"
  sh "cp ${build_root}/${package_name}_*.tar.xz ${pool_dir}"
  sh "cp ${build_root}/${package_name}_*.dsc ${pool_dir}"
  dir("${repo_dir}")
  {
    // Generate package lists
    sh "mkdir -p ${dists_bin_dir}"
    sh "dpkg-scanpackages --arch amd64 -m pool/${component} > ${dists_bin_dir}/Packages"
    sh "cat ${dists_bin_dir}/Packages | gzip -9c > ${dists_bin_dir}/Packages.gz"

    // Generate source lists
    sh "mkdir -p ${dists_src_dir}"
    sh "dpkg-scansources pool/ > ${dists_src_dir}/Sources"
    sh "cat ${dists_src_dir}/Sources | gzip -9c > ${dists_src_dir}/Sources.gz"
  }
  sh """
    if [ -d "${repo_dir}@tmp" ];
    then
      rmdir "${repo_dir}@tmp"
    fi
  """
}

def generate_debian_release_file(String ci_root, String distro)
{
  def repo_dir="/usr/share/nginx/repo.karlofduty.com/${distro}"

  dir("${repo_dir}/dists/${distro}")
  {
    sh "${ci_root}/scripts/generate-deb-release-file.sh > Release"
    sh "cat Release | gpg -u 2FEAAE97C813C486 -abs > Release.gpg"
    sh "gpg --verify Release.gpg Release"
    sh "cat Release | gpg -u 2FEAAE97C813C486 --clearsign > InRelease"
    sh "gpg --verify InRelease"
  }
  sh """
    if [ -d "${repo_dir}/dists/${distro}@tmp" ];
    then
      rmdir "${repo_dir}/dists/${distro}@tmp"
    fi
  """
}

return this