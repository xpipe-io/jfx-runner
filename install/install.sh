#!/usr/bin/env bash

info() {
  local action="$1"
  local details="$2"
  command printf '\033[1;32m%12s\033[0m %s\n' "$action" "$details" 1>&2
}

error() {
  command printf '\033[1;31mError\033[0m: %s\n\n' "$1" 1>&2
}

install() {
  local uname_str="$(uname -s)"
  local dir="$(dirname "$0")"

  case "$uname_str" in
  Linux)
    if [ -f "/etc/debian_version" ]; then
      xargs sudo apt install < "$dir/debian_packages.txt"
    fi
    ;;
  Darwin)
    sudo installer -verboseR -pkg "$file" -target /
    ;;
  *)
    exit 1
    ;;
  esac
}

install

exit_status="$?"
if [ "$exit_status" != 0 ]; then
  error "Installation failed."
  exit "$exit_status"
fi
