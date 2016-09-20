set :application, 'solrmarc-sw'
set :repo_url, 'https://github.com/sul-dlss/solrmarc-sw.git'
set :user, 'blacklight'

# Default branch is :master
ask :branch, proc { `git rev-parse --abbrev-ref HEAD`.chomp }.call

set :home_directory, "/opt/app/#{fetch(:user)}"
set :deploy_to, "#{fetch(:home_directory)}/#{fetch(:application)}"

# Default value for keep_releases is 5
set :keep_releases, 5

namespace :deploy do
  desc "Run ant to build service."
  task :run_ant do
    on roles(:app) do
      within release_path do
        execute :ant, "dist_site"
      end
    end
  end
end
after 'deploy:published', 'deploy:run_ant'
