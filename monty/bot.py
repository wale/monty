import os
from datetime import datetime

import discord
from discord.ext import bridge, commands
from loguru import logger

from monty.util.config import Config
from monty.util.traceback import log_traceback_maker


class MontyBot(bridge.AutoShardedBot):
    def __init__(self, *args, prefix: str, config: dict, **kwargs):
        super().__init__(*args, command_prefix=prefix, **kwargs)  # type: ignore
        self.prefix = prefix
        self.uptime: datetime = None  # type: ignore
        self.config = config

    @commands.Cog.listener()
    async def on_connect(self) -> None:
        logger.info("Starting Monty...")
        try:
            for file in os.listdir("monty/cogs"):
                if file.endswith(".py") and not file.startswith("__init__"):
                    name = file[:-3]
                    self.load_extension(f"monty.cogs.{name}")
                    self.auto_sync_commands = True
        except Exception as e:
            logger.error(f"Could not setup hook: \n{log_traceback_maker(e)}")
