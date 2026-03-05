param(
  [string]$BaseUrl = "http://localhost:2345"
)

$paths = @(
  "/",
  "/products",
  "/categories",
  "/profile",
  "/orders",
  "/login",
  "/register",
  "/user/profile",
  "/user/orders",
  "/admin/dashboard",
  "/admin/categories",
  "/admin/orders"
)

Write-Host "Checking endpoints on $BaseUrl" -ForegroundColor Cyan

foreach ($p in $paths) {
  try {
    $res = Invoke-WebRequest -Uri ($BaseUrl + $p) -UseBasicParsing -MaximumRedirection 0 -ErrorAction Stop
    $code = [int]$res.StatusCode
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
      $code = [int]$_.Exception.Response.StatusCode
    } else {
      $code = "ERROR"
    }
  }

  $status =
    if ($code -eq 200 -or $code -eq 302 -or $code -eq 301) { "OK" }
    elseif ($code -eq 401 -or $code -eq 403) { "AUTH" }
    else { "FAIL" }

  "{0,-18} {1,5} {2}" -f $p, $code, $status
}

