# server uses standardized suffix
server "solrmarc-stage.stanford.edu", user: fetch(:user), roles: %w{app}
Capistrano::OneTimeKey.generate_one_time_key!
