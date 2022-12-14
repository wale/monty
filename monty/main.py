import asyncio
import os

import discord
from loguru import logger

from monty.bot import MontyBot
from monty.util.config import Config
from monty.util.help import MontyHelpCommand

config_class = Config()
config_class.load()

config = config_class.get_config()


async def run() -> None:
    bot = MontyBot(
        config=config,
        prefix=config["bot"]["prefixes"],
        owner_ids=config["bot"]["owners"],
        allowed_mentions=discord.AllowedMentions(
            roles=False, users=True, everyone=False
        ),
        intents=discord.Intents.all(),
        help_command=MontyHelpCommand(),
    )

    try:
        await bot.start(config["bot"]["token"])
    except Exception as e:
        print(f"Error when logging in: {e}")


def main() -> None:
    loop = asyncio.get_event_loop()
    try:
        loop.run_until_complete(run())
    except Exception as e:
        logger.error(f"Bot errorred. {e}")
    except KeyboardInterrupt as e:
        print("\t")
        logger.error(f"Keyboard interrupt call caught. Exiting.")
        exit(0)


if __name__ == "__main__":
    main()
