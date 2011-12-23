#!/usr/bin/env python
# Script to automate installation of TI Android SGX SDK
# This is an alternative to internal_install_SDK.exp expect script benefiting
# from the better PTY management of pexpect to support automated builds. To
# keep everything nicely in the build directory, we bypass the Makefile.
# Released to public domain by Ash Charles <ash@gumstix.com> (2011)
import sys
import os
import pexpect
import subprocess

SRCDIR = os.path.join(os.getcwd(), 'external/ti_android_sgx_sdk')
INSTALLER = 'OMAP35x_Android_Graphics_SDK_setuplinux_3_01_00_03.bin'
TARGETDIR = os.path.join(os.getcwd(), 'out/target/product/overo')
DESTDIR = os.path.join(TARGETDIR, 'SDK')
OMAPES = 'OMAPES=3.x'
KERNEL_DIR = os.path.join(os.getcwd(), 'kernel')
TOOLS_PREFIX = os.path.join(os.getcwd(),
    'prebuilt/linux-x86/toolchain/arm-eabi-4.4.3/bin/arm-eabi-')

def apply_patches():
    """Apply patches to tweak SDK build system."""
    with open(os.path.join(os.getcwd(), 'utils', 'sdk.patch'), 'r') as fin:
        subprocess.call(['patch', '-p2'], stdin=fin, cwd=DESTDIR)
    with open(os.path.join(SRCDIR, 's-video_sgx.patch'), 'r') as fin:
        subprocess.call(['patch', '-p2'], stdin=fin, cwd=DESTDIR)

def prepare():
    """Makefile 'prepare' target."""
    args = ' --mode console'
    child = pexpect.spawn(os.path.join(SRCDIR, INSTALLER) + args)
    child.logfile = sys.stdout
    child.expect("This will install TI OMAP35xx/37xx Android Graphics SDK")
    child.sendline('Y')
    child.expect("This will install TI OMAP35xx/37xx Android Graphics SDK")
    child.sendline('Y')
    child.expect("-- Press space to continue or 'q' to quit --")
    child.sendline('q')
    child.expect("Where do you want to install")
    child.sendline(DESTDIR)
    child.expect(pexpect.EOF, timeout=120)
    apply_patches()

def build():
    """Makefile 'build' target."""
    env = os.environ
    env.update({'INSTALL_DIR' : DESTDIR,
                'PRODUCT_DIR' : TARGETDIR,
                'KERNEL_DIR' : KERNEL_DIR,
                'TOOLS_PREFIX' : TOOLS_PREFIX,
                'PWD' : DESTDIR, })
    subprocess.call(['make', '-C', DESTDIR], env=env)

def install():
    """Makefile 'install' target."""
    subprocess.call(['make', '-C', DESTDIR, 'install_km', OMAPES])

if __name__ == "__main__":
    prepare()
    build()
    install()
