# 00 - Setup your environment

__This guide is part of the [Build, Run and Monitor Intelligent Java Apps on Azure Container Apps and Azure OpenAI](../README.md)__

In this section, we'll set up everything you need to expeditiously complete the lab.

---

## Prerequisites

This training lab requires the following to be installed on your machine:

* [JDK 17](https://docs.microsoft.com/java/openjdk/download#openjdk-17)
* A text editor or an IDE. If you do not already have an IDE for Java development, we recommend using [Visual Studio Code](https://code.visualstudio.com) with the [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).

* The Bash shell. While Azure CLI should behave identically on all environments, shell semantics vary. Therefore, only bash can be used with the commands in this training. To complete this training on Windows, use [Git Bash that accompanies the Windows distribution of Git](https://git-scm.com/download/win). **Use only Git Bash to complete this training on Windows. Do not use WSL, CloudShell, or any other shell.**

* [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest) version 2.51.0 or later. You can check the version of your current Azure CLI installation by running:

  ```bash
  az --version
  ```

> 💡 If you try the command above and you see the error `bash: az: command not found`, run the following command: `alias az='az.cmd'` and try again.

* 🚧 The `containerapp` extension for Azure CLI. You can install or update this extension after installing Azure CLI by running `az extension add --name containerapp --upgrade`.

> 💡 In sections 9 and 10, you will access the UI of the Microservice applications in a web browser. Use the [Microsoft Edge](https://microsoft.com/edge), Google Chrome, or Firefox for these sections.

The environment variable `JAVA_HOME` should be set to the path of the JDK installation. The directory specified by this path should have `bin`, `jre`, and `lib` among its subdirectories. Further, ensure your `PATH` variable contains the directory `${JAVA_HOME}/bin`. To test, type `which javac` into bash shell ensure the resulting path points to a file inside `${JAVA_HOME}/bin`.

You can then use Visual Studio Code or an IDE of your choice.

---

➡️ Next guide: [01 - Build a simple Java application](../01-build-a-simple-java-application/README.md)