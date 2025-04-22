def publish_deb_package(String distro, String package_name, String package_dir, String build_root)
{
  def repo_dir="/usr/share/nginx/repo.karlofduty.com/${distro}"
  def pool_dir="${repo_dir}/pool/dev/${package_dir}"
  def dists_bin_dir="${repo_dir}/dists/${distro}/dev/binary-amd64"
  def dists_src_dir="${repo_dir}/dists/${distro}/dev/source"

  // Copy package and sources to pool directory
  sh "mkdir -p ${pool_dir}"
  sh "cp ${build_root}/${package_name}_*_amd64.deb ${pool_dir}"
  sh "cp ${build_root}/${package_name}_*.tar.xz ${pool_dir}"
  sh "cp ${build_root}/${package_name}_*.dsc ${pool_dir}"
  dir("${repo_dir}")
  {
    // Generate package lists
    sh "mkdir -p ${dists_bin_dir}"
    sh "dpkg-scanpackages --arch amd64 -m pool/ > ${dists_bin_dir}/Packages"
    sh "cat ${dists_bin_dir}/Packages | gzip -9c > ${dists_bin_dir}/Packages.gz"

    // Generate source lists
    sh "mkdir -p ${dists_src_dir}"
    sh "dpkg-scansources pool/ > ${dists_src_dir}/Sources"
    sh "cat ${dists_src_dir}/Sources | gzip -9c > ${dists_src_dir}/Sources.gz"
  }
}

def generate_debian_release_file(String ci_root, String distro)
{
  def repo_dir="/usr/share/nginx/repo.karlofduty.com/${distro}"

  dir("${repo_dir}/dists/${distro}")
  {
    sh "${ci_root}/scripts/generate-deb-release-file.sh > Release"
    withCredentials([string(credentialsId: 'JENKINS_GPG_KEY_PASSWORD', variable: 'JENKINS_GPG_KEY_PASSWORD')]) {
      sh '/usr/lib/gnupg/gpg-preset-passphrase --passphrase "$JENKINS_GPG_KEY_PASSWORD" --preset 0D27E4CD885E9DD79C252E825F70A1590922C51E'
      sh "cat Release | gpg --default-key 'Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>' -abs > Release.gpg"
      sh "cat Release | gpg --default-key 'Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>' -abs --clearsign > InRelease"
    }
  }
}

return this