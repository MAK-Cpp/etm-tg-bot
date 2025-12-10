#!/bin/sh
echo "starting container $hostname"
if [ -z "$BOT_TOKEN" ]; then
  echo "Container failed to start, pls pass -e BOT_TOKEN=bot-token"
  exit 1
fi
echo "starting container with $BOT_TOKEN"
#your long-running command from CMD
exec java -DBOT_TOKEN=$BOT_TOKEN -jar /bot.jar