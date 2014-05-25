package actors.domain

case class GithubIssue(url: String,
                       labels_url: String,
                       comments_url: String,
                       events_url: String,
                       html_url: String,
                       id: Int,
                       number: Int,
                       title: String,
                       body: String,
                       user: GithubUser,
                       labels: Seq[GithubLabel],
                       state: String,
                       assignee: GithubUser,
                       milestone: GithubMilestone,
                       comments: Int,
                       pull_request: GithubPullRequest,
                       closed_at: String,
                       created_at: String,
                       updated_at: String)

case class GithubUser(login: String,
                      id: Int,
                      avatar_url: String,
                      gravatar_id: String,
                      url: String,
                      html_url: String,
                      followers_url: String,
                      following_url: String,
                      gists_url: String,
                      starred_url: String,
                      subscriptions_url: String,
                      organizations_url: String,
                      repos_url: String,
                      events_url: String,
                      received_events_url: String,
                      user_type: String,
                      site_admin: Boolean)

case class GithubLabel(url: String, name: String, color: String)

case class GithubMilestone(url: String,
                           number: Int,
                           state: String,
                           title: String,
                           description: String,
                           creator: GithubUser,
                           open_issues: Int,
                           closed_issues: Int,
                           created_at: String,
                           updated_at: String,
                           due_on: Boolean)

case class GithubPullRequest(url: String, html_url: String, diff_url: String, patch_url: String)
