(defproject org.clojars.jj/bump-helm "1.0.0-SNAPSHOT"
  :description "A leiningen plugin to set the project version in a Helm Chart.yaml file"
  :url "https://github.com/ruroru/lein-bump-helm"
  :license {:name "EPL-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]]

  :deploy-repositories [["clojars" {:url           "https://repo.clojars.org"
                                    :username      :env/clojars_user
                                    :password      :env/clojars_pass
                                    :sign-releases false}]]

  :profiles {:test {:dependencies   [[mock-clj "0.2.1"]]
                    :resource-paths ["test/resources"]}}

  :plugins [[org.clojars.jj/bump "1.0.4"]
            [org.clojars.jj/strict-check "1.1.0"]
            [org.clojars.jj/lein-git-tag "1.0.1"]
            [org.clojars.jj/bump-md "1.1.0"]])
