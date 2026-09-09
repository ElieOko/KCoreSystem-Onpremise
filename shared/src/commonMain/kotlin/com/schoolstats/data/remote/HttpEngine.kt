package com.schoolstats.data.remote

import io.ktor.client.engine.HttpClientEngine

expect fun createHttpEngine(): HttpClientEngine
