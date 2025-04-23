pipeline
{
  agent any
  stages
  {
    stage("Preparation")
    {
      steps
      {
        script
        {
          common = load("${env.WORKSPACE}/scripts/common.groovy")
        }
      }
    }
    stage('Build / Package')
    {
      parallel
      {
        stage('RHEL')
        {
          agent
          {
            dockerfile { filename 'docker/RHEL8.Dockerfile' }
          }
          steps
          {
            sh 'rpmbuild -bb rpm-repos/karlofduty-repo.spec --define "_topdir $PWD/rhel" --define "distro rhel"'
            sh 'cp rhel/RPMS/x86_64/karlofduty-repo-*.x86_64.rpm rhel/'
            stash includes: 'rhel/karlofduty-repo-*.x86_64.rpm', name: 'rhel-rpm'
          }
        }
        stage('Fedora')
        {
          agent
          {
            dockerfile { filename 'docker/Fedora.Dockerfile' }
          }
          steps
          {
            sh 'rpmbuild -bb rpm-repos/karlofduty-repo.spec --define "_topdir $PWD/fedora" --define "distro fedora"'
            sh 'cp fedora/RPMS/x86_64/karlofduty-repo-*.x86_64.rpm fedora/'
            stash includes: 'fedora/karlofduty-repo-*.x86_64.rpm', name: 'fedora-rpm'
          }
        }
        stage('Debian')
        {
          agent
          {
            dockerfile { filename 'docker/Debian.Dockerfile' }
          }
          environment
          {
            DEBEMAIL="xkaess22@gmail.com"
            DEBFULLNAME="Karl Essinger"
            DISTRO="debian"
            PACKAGE_ROOT="${WORKSPACE}/debian"
          }
          steps
          {
            sh './deb-repos/generate-deb.sh'
            archiveArtifacts(artifacts: 'debian/karlofduty-repo_*_amd64.deb, debian/karlofduty-repo_*.tar.xz', caseSensitive: true)
            stash includes: 'debian/karlofduty-repo_*_amd64.deb, debian/karlofduty-repo_*.tar.xz, debian/karlofduty-repo_*.dsc', name: 'debian-deb'
          }
        }
        stage('Ubuntu')
        {
          agent
          {
            dockerfile { filename 'docker/Ubuntu.Dockerfile' }
          }
          environment
          {
            DEBEMAIL="xkaess22@gmail.com"
            DEBFULLNAME="Karl Essinger"
            DISTRO="ubuntu"
            PACKAGE_ROOT="${WORKSPACE}/ubuntu"
          }
          steps
          {
            sh './deb-repos/generate-deb.sh'
            archiveArtifacts(artifacts: 'ubuntu/karlofduty-repo_*_amd64.deb, ubuntu/karlofduty-repo_*.tar.xz', caseSensitive: true)
            stash includes: 'ubuntu/karlofduty-repo_*_amd64.deb, ubuntu/karlofduty-repo_*.tar.xz, ubuntu/karlofduty-repo_*.dsc', name: 'ubuntu-deb'
          }
        }
      }
    }
    stage('Sign')
    {
      parallel
      {
        stage('RHEL')
        {
          steps
          {
            unstash name: 'rhel-rpm'
            withCredentials([string(credentialsId: 'JENKINS_GPG_KEY_PASSWORD', variable: 'JENKINS_GPG_KEY_PASSWORD')]) {
              sh '/usr/lib/gnupg/gpg-preset-passphrase --passphrase "$JENKINS_GPG_KEY_PASSWORD" --preset 0D27E4CD885E9DD79C252E825F70A1590922C51E'
              sh 'rpmsign --define "_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>" --addsign rhel/karlofduty-repo-*.x86_64.rpm'
              sh 'rpm -vv --checksig rhel/karlofduty-repo-*.x86_64.rpm'
            }
            archiveArtifacts(artifacts: 'rhel/karlofduty-repo-*.x86_64.rpm', caseSensitive: true)
          }
        }
        stage('Fedora')
        {
          steps
          {
            unstash name: 'fedora-rpm'
            withCredentials([string(credentialsId: 'JENKINS_GPG_KEY_PASSWORD', variable: 'JENKINS_GPG_KEY_PASSWORD')]) {
              sh '/usr/lib/gnupg/gpg-preset-passphrase --passphrase "$JENKINS_GPG_KEY_PASSWORD" --preset 0D27E4CD885E9DD79C252E825F70A1590922C51E'
              sh 'rpmsign --define "_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>" --addsign fedora/karlofduty-repo-*.x86_64.rpm'
              sh 'rpm -vv --checksig fedora/karlofduty-repo-*.x86_64.rpm'
            }
            archiveArtifacts(artifacts: 'fedora/karlofduty-repo-*.x86_64.rpm', caseSensitive: true)
          }
        }
      }
    }
    stage('Deploy')
    {
      parallel
      {
        stage('RHEL')
        {
          when
          {
            expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
          }
          steps
          {
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/rhel/el8/packages/karlofduty-repo/'
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/rhel/el9/packages/karlofduty-repo/'
            sh 'cp rhel/karlofduty-repo-*.x86_64.rpm /usr/share/nginx/repo.karlofduty.com/rhel/el8/packages/karlofduty-repo/'
            sh 'cp rhel/karlofduty-repo-*.x86_64.rpm /usr/share/nginx/repo.karlofduty.com/rhel/el9/packages/karlofduty-repo/'
            sh 'rm /usr/share/nginx/repo.karlofduty.com/rhel/karlofduty-repo-latest.x86_64.rpm || echo "Link to latest package didn\'t exist"'
            sh 'ln -s /usr/share/nginx/repo.karlofduty.com/rhel/el8/packages/karlofduty-repo/$(cd rhel && ls karlofduty-repo-*.x86_64.rpm) /usr/share/nginx/repo.karlofduty.com/rhel/karlofduty-repo-latest.x86_64.rpm'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/rhel/el8'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/rhel/el9'
          }
        }
        stage('Fedora')
        {
          when
          {
            expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
          }
          steps
          {
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/fedora/packages/karlofduty-repo/'
            sh 'cp fedora/karlofduty-repo-*.x86_64.rpm /usr/share/nginx/repo.karlofduty.com/fedora/packages/karlofduty-repo/'
            sh 'rm /usr/share/nginx/repo.karlofduty.com/fedora/karlofduty-repo-latest.x86_64.rpm || echo "Link to latest package didn\'t exist"'
            sh 'ln -s /usr/share/nginx/repo.karlofduty.com/fedora/packages/karlofduty-repo/$(cd fedora && ls karlofduty-repo-*.x86_64.rpm) /usr/share/nginx/repo.karlofduty.com/fedora/karlofduty-repo-latest.x86_64.rpm'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/fedora'
          }
        }
        stage('Debian')
        {
          when
          {
            expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
          }
          environment
          {
            DISTRO="debian"
            PACKAGE_NAME="karlofduty-repo"
            COMPONENT="main"
          }
          steps
          {
            unstash name: "${env.DISTRO}-deb"
            script
            {
              common.publish_deb_package(env.DISTRO, env.PACKAGE_NAME, env.PACKAGE_NAME, "${WORKSPACE}/${env.DISTRO}", env.COMPONENT)
              common.generate_debian_release_file("${WORKSPACE}", env.DISTRO)
            }
            sh "rm /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/dists/${env.DISTRO}/karlofduty-repo_latest_amd64.deb || echo \"Link to latest package didn't exist\""
            sh "ln -s /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/pool/${env.COMPONENT}/karlofduty-repo/\$(cd ${env.DISTRO} && ls karlofduty-repo_*_amd64.deb) /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/dists/${env.DISTRO}/karlofduty-repo_latest_amd64.deb"
          }
        }
        stage('Ubuntu')
        {
          when
          {
            expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
          }
          environment
          {
            DISTRO="ubuntu"
            PACKAGE_NAME="karlofduty-repo"
            COMPONENT="main"
          }
          steps
          {
            unstash name: "${env.DISTRO}-deb"
            script
            {
              common.publish_deb_package(env.DISTRO, env.PACKAGE_NAME, env.PACKAGE_NAME, "${WORKSPACE}/${env.DISTRO}", env.COMPONENT)
              common.generate_debian_release_file("${WORKSPACE}", env.DISTRO)
            }
            sh "rm /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/dists/${env.DISTRO}/karlofduty-repo_latest_amd64.deb || echo \"Link to latest package didn't exist\""
            sh "ln -s /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/pool/${env.COMPONENT}/karlofduty-repo/\$(cd ${env.DISTRO} && ls karlofduty-repo_*_amd64.deb) /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/dists/${env.DISTRO}/karlofduty-repo_latest_amd64.deb"
          }
        }
      }
    }
  }
}
