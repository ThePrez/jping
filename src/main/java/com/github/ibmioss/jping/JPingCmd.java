package com.github.ibmioss.jping;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.github.ibmioss.jping.StringUtils.TerminalColor;

/**
 * Main entry point for the application
 *
 * @author Jesse Gorzinski
 */
public class JPingCmd {

    public static void main(final String... _args) {
        final LinkedList<String> args = new LinkedList<String>(Arrays.asList(_args));

        String hostname = null;
        long numTries = 5;
        int ttl = 255;
        int timeout = 1000;
        String localAddr = null;
        try {
            while (!args.isEmpty()) {
                String arg = args.removeFirst();
                if ("-h".equals(arg) || "--help".equals(arg)) {
                    printUsageAndExit();
                } else if ("-i".equals(arg)) {
                    ttl = Integer.valueOf(args.removeFirst());
                } else if ("-n".equals(arg)) {
                    numTries = Integer.valueOf(args.removeFirst());
                } else if ("-t".equals(arg)) {
                    numTries = Long.MAX_VALUE;
                } else if ("-w".equals(arg)) {
                    timeout = Integer.valueOf(args.removeFirst());
                } else if ("-S".equals(arg)) {
                    localAddr = args.removeFirst();
                } else if (arg.startsWith("-")) {
                    System.err.println(StringUtils.colorizeForTerminal("ERROR: Unknown option '" + arg + "'", TerminalColor.BRIGHT_RED));
                    printUsageAndExit();
                } else {
                    hostname = arg;
                }
            }
        } catch (NoSuchElementException | NumberFormatException e) {
            printUsageAndExit();
        }

        if (null == hostname) {
            printUsageAndExit();
        }

        NetworkInterface nf;
        if (null == localAddr) {
            nf = null;
        } else {
            try {
                nf = NetworkInterface.getByInetAddress(InetAddress.getByName(localAddr));
            } catch (Exception e) {
                nf = null;
                System.err.println(StringUtils.colorizeForTerminal(String.format("ERROR: Cannot resolve interface for address '%s'", localAddr), TerminalColor.BRIGHT_RED));
                printUsageAndExit();
            }
        }

        InetAddress addr = null;
        int failures = 0;
        int successes = 0;
        try {
            addr = InetAddress.getByName(hostname);
            System.out.printf("Pinging %s [%s] with 32 bytes of data:\n", addr.getCanonicalHostName(), "" + addr.getHostAddress());
            for (long i = 1; i <= numTries; ++i) {
                PingResult result = ping(addr, nf, ttl, timeout);
                result.println(System.out);
                if (result.isSuccess()) {
                    successes++;
                } else {
                    failures++;
                }
                try {
                    Thread.sleep(333);
                } catch (Exception e) {
                }
            }
            System.out.print(StringUtils.colorizeForTerminal(String.format("%d of %d attempts were successful\n", successes, successes + failures), TerminalColor.CYAN));
        } catch (UnknownHostException e) {
            System.err.println(StringUtils.colorizeForTerminal(String.format("ERROR: Unknown host '%s'", hostname), TerminalColor.RED));
        }

    }

    private static PingResult ping(InetAddress _addr, NetworkInterface _intf, int _ttl, int _timeout) {
        try {
            final long startTs = System.currentTimeMillis();
            boolean isReachable = _addr.isReachable(_intf, _ttl, _timeout);
            final long endTs = System.currentTimeMillis();
            final long duration = endTs - startTs;
            if(duration > (2+_timeout)) {
                return new PingResult(false, -1, _addr, null);
            }
            return new PingResult(isReachable, Math.min(_timeout, duration), _addr, null);
        } catch (IOException e) {
            return new PingResult(false, -1, _addr, e);
        }

    }

    private static void printUsageAndExit() {
        // @formatter:off
		final String usage = "Usage: jping [options] <host>\n"
		                        + "    Valid options include:\n"
                                + "        -i <ttl>          Ping 'time to live' (default: 255)\n"
                                + "        -n <number>       Number of tries (default: 5)\n"
                                + "        -t                Ping the specified host until stopped.\n"
                                + "        -w <timeout>      Ping timeout, in ms (default: 1000)\n"
                                + "        -S <address>      Set source address to specified interface address\n"
                                ;
		// @formatter:on
        System.err.println(usage);
        System.exit(-1);
    }

    private static class PingResult {

        private boolean m_success;
        private long m_time;
        private InetAddress m_addr;
        private IOException m_exc;
        private int m_bytes = 32;

        public PingResult(boolean _b, long _time, InetAddress _addr, IOException _e) {
            m_success = _b && null == _e;
            m_time = _time;
            m_addr = _addr;
            m_exc = _e;

        }

        public boolean isSuccess() {
            return m_success;
        }

        public void println(PrintStream _out) {
            if (null != m_exc) {
                _out.println(StringUtils.colorizeForTerminal("Network Error: " + m_exc.getLocalizedMessage(), TerminalColor.RED));
            } else if (!m_success) {
                _out.println(StringUtils.colorizeForTerminal("Request timed out.", TerminalColor.RED));
            } else {
                _out.println(StringUtils.colorizeForTerminal(String.format("Reply from %s: bytes=%d time=%sms", m_addr.getHostAddress(), m_bytes, m_time), TerminalColor.GREEN));
            }
        }

    }
}
