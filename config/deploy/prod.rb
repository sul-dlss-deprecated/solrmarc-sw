# server uses standardized suffix
server "solrmarc-prod.stanford.edu", user: fetch(:user), roles: %w{app}
Capistrano::OneTimeKey.generate_one_time_key!
