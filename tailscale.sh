#!/usr/bin/bash

tailscaled --tun=userspace-networking &

tailscale up &

tailscale ip -4