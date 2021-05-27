# AccountAPI
AccountAPI is a lightweight API to log into Microsoft and Mojang Accounts for Minecraft.

While the Mojang Accounts are pretty simple, fast and straight forward, the Microsoft Accounts require a Browser and 6 connections to different servers. 
That makes the Microsoft Account authentication slow and should not be done on the Main Thread

## How-To
Simply call AccountAPI#create() before doing anything else.
From here you can either create a `new MojangAccount()` with email and password, or via cached access token.
Or you can create a `new MicrosoftAccount()` which opens a browser, and local http server where you have to login via Microsoft Login Page

## Login Process
To join a minecraft servers you need an access token, which is being verified by the Minecraft Servers.
For Mojang Accounts you can send the Credentials to `authserver.mojang.com/authenticate` and get the Access Token directly, or refresh an invalidates one, by going to `https://authserver.mojang.com/refresh`

For Microsoft Accounts that is a bit more complicated, here is my best try at explaining it:

You send the Credentials via Browser to `login.live.com/oauth20_authorize.srf` with your Azure Client Id and obtain an auth code.
Now you send that auth code to `login.live.com/oauth20_token.srf?scope=XboxLive.signin%20offline_access` and recieve an auth token for the XBox Live Services.
We then get an XBL Token from `user.auth.xboxlive.com/user/authenticate` using the auth code.
We convert that to an XSTS Token via `xsts.auth.xboxlive.com/xsts/authorize` by entering the XBL Token

Now that we have the XSTS Token we have to send that to the Mojang Api ._.
First we obtain the Access Token for all the Mojang Servers by sending the XSTS Token to `api.minecraftservices.com/authentication/login_with_xbox`.
We could join a Server now, but we don't know the players name and uuid, nor if he even owns the Game.
So to check whether the Players owns the Game or not, we send the access token to the Mojang Servers once again `api.minecraftservices.com/entitlements/mcstore`.
If he owns the Game we can check the Profile from the next Mojang Server Call to `api.minecraftservices.com/minecraft/profile`.

Woo, we did it! We now have access to everything that the Minecraft Client needs to start.