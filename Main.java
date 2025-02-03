
import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
public class Main
{
    public static void main(String[] args)
    {
        //Initialize the values
        int pc = 512;
        int i = 0;
        int x;
        int y;
        //Get the ROM in hex
        //System.out.println("Paste Hex-converted CHIP8 rom now:");
        //String rominput = Keyboard.getString();
        int[] memory = new int[65536];
        int[] display = new int[2048];
        int[] registers = new int[16];
        int[] stack = new int[16];
        int opone;
        int optwo;
        int delaytimer = 0;
        int soundtimer = 0;
        int sp = -1;
        System.out.println("Memory init");
        //file i/o
        if (args.length < 1) {
            System.out.println("Please provide CHIP-8 ROM!");
            System.exit(0);
        }
        System.out.println("File detected");
        String inputFile = args[0];
 
        try (
            InputStream inputStream = new FileInputStream(inputFile);
        ) {
            System.out.println("Getting length");
            long fileSize = new File(inputFile).length();
            byte[] allBytes = new byte[(int) fileSize];
 
            int bytesRead = inputStream.read(allBytes);
 
            for(int rk = 0; rk < allBytes.length; rk++) {
                memory[rk + 512] = (allBytes[rk] & 0xFF);
             }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Starting emulation");
        //Start the main loop
        while (true)
        {
            
            //Test opcodes
            //memory[512] = 0;
            //memory[513] = 224;
            //memory[514] = 0;
            //memory[515] = 253;
            //CPU
            for (int cycles = 1; cycles <= 32; cycles++)
            {
            opone = memory[pc];
            optwo = memory[pc+1];  //fetch opcodes from memory
            pc += 2;
            x = opone % 16;
            y = optwo / 16;
            //Begin Instruction decoding
            if (opone == 0) //00xx range
            {
                if (optwo == 224) //00E0 Clear Screen
                {
                    Arrays.fill(display, 0);
                    continue; //Escape the cycles and draw now
                }
                if (optwo == 238) //00EE Return from Subroutine
                {
                    pc = stack[sp];
                    sp--;
                    continue;
                }
                if (optwo == 253) //00FD Exit
                {
                    System.exit(0);
                }
                continue;
            }
            if (opone <= 32) //1NNN Jump
            {
                pc = optwo + (256 * (opone % 16));
                continue;
            }
            if (opone <= 48) //2NNN Call Subroutine
            {
                sp++;
                stack[sp] = pc;
                pc = optwo + (256 * (opone % 16));
                continue;
            }
            if (opone <= 64) //3XNN Skip next instruction if VX == NN
            {
                if (registers[x] == optwo)
                {
                    if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0))) //For XO-CHIP's double skips.
                    {
                        pc += 2;
                    }
                    pc += 2;
                }
                continue;
            }
            if (opone <= 80) //4XNN Skip next instruction if VX != NN
            {
                if (registers[x] != optwo)
                {
                    if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0)))
                    {
                        pc += 2;
                    }
                    pc += 2;
                }
                continue;
            }
            if (opone <= 96) 
            {
                if ((optwo % 16) == 0) //5XY0 Skip if VX == VY
                {
                    if (registers[x] == registers[y])
                    {
                        if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0)))
                        {
                            pc += 2;
                        }
                        pc += 2;
                    }
                    continue;
                }
                if ((optwo % 16) == 1) //5XY1 (HWC-64) Skip if VX > VY
                {
                    if (registers[x] > registers[y])
                    {
                        if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0))) 
                        {
                            pc += 2;
                        }
                        pc += 2;
                    }
                    continue;
                }
                if ((optwo % 16) == 2) //5XY2
                {

                    continue;
                }
                if ((optwo % 16) == 3) //5XY3
                {

                }
                continue;
            }
            if (opone <= 112) //6XNN Set VX to NN
            {
                registers[x] = optwo;
                continue;
            }
            if (opone <= 128) //7XNN Add NN to VX
            {
                registers[x] = (registers[x] + optwo) % 256;
                continue;
            }
            if (opone <= 144) //8XY0 Set VX to VY
            {
                if ((optwo % 16) == 0) //8XY0 Set VX to VY
                {
                    registers[x] = registers[y];
                    continue;
                }
                if ((optwo % 16) == 1) //8XY1 Set VX to VX OR VY
                {
                    registers[x] = registers[x] | registers[y];
                    continue;
                }
                if ((optwo % 16) == 2) //8XY2 Set VX to VX AND VY
                {
                    registers[x] = registers[x] & registers[y];
                    continue;
                }
                if ((optwo % 16) == 3) //8XY3 Set VX to VX XOR VY
                {
                    registers[x] = registers[x] ^ registers[y];
                    continue;
                }
                if ((optwo % 16) == 4) //8XY4 Add VY to VX
                {
                    int dummy = registers[x] + registers[y];
                    registers[x] = dummy % 256;
                    registers[15] = (dummy > 255) ? 1 : 0;
                    continue;
                }
                if ((optwo % 16) == 5) //8XY5 Subtract VY from VX
                {
                    int dummy = registers[x] - registers[y];
                    registers[x] = dummy % 256;
                    registers[15] = (dummy < 0) ? 0 : 1;
                    continue;
                }
                if ((optwo % 16) == 6) //8XY6 Set VX to VY shifted right by 1 (VIP Style)
                {
                    int dummy = registers[y];
                    registers[x] = registers[y] >> 1;
                    registers[15] = dummy % 2;
                    continue;
                }
                if ((optwo % 16) == 7) //8XY7 Subtract VX from VY
                {
                    int dummy = registers[y] - registers[x];
                    registers[x] = dummy % 256;
                    registers[15] = (dummy < 0) ? 0 : 1;
                    continue;
                }if ((optwo % 16) == 7) //8XY7 Subtract VX from VY
                {
                    int dummy = registers[y] - registers[x];
                    registers[x] = dummy % 256;
                    registers[15] = (dummy < 0) ? 0 : 1;
                    continue;
                }
                if ((optwo % 16) == 13) //8XYD
                {
                    int dummy = registers[x] % registers[y];
                    registers[x] = registers[x] / registers[y];
                    registers[15] = dummy;
                    continue;
                }
                if ((optwo % 16) == 14) //8XYE Set VX to VY shifted left by 1 (VIP Style)
                {
                    int dummy = registers[y];
                    registers[x] = registers[y] << 1;
                    registers[15] = (dummy > 127) ? 1 : 0;
                }
                continue;

            }
            if (opone <= 160) //9XY0 Skip if VX != VY
            {
                if (registers[x] != registers[y])
                {
                    if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0)))
                    {
                        pc += 2;
                    }
                    pc += 2;
                }
                continue;
            }
            if (opone <= 176) //ANNN Set I to NNN
            {
                i = optwo + (256 * (opone % 16));
                continue;
            }
            if (opone <= 192) //BNNN Jump to NNN + V0
            {
                pc = optwo + (256 * (opone % 16)) + registers[0];
                continue;
            }
            if (opone <= 208) //CXNN Set VX to random number AND NN
            {
                registers[x] = (int)(Math.random() * 255) & optwo;
                continue;
            }
            if (opone <= 224) //DXYN Draw sprite at VX, VY with height N
            {
                int height = optwo % 16;
                if (height == 0)
                {
                    height = 16;
                }
                int pixel;
                int position = i;
                registers[15] = 0;
                for (int linevert = 0; linevert < height; linevert++)
                {
                    pixel = memory[position + linevert];
                    for (int linehoriz = 0; linehoriz < 8; linehoriz++)
                    {
                        if ((pixel & (128 >> linehoriz)) != 0)
                        {
                            if (display[(registers[x] + linehoriz + ((registers[y] + linevert) * 64))] == 1)
                            {
                                registers[15] = 1;
                            }
                            display[(registers[x] + linehoriz + ((registers[y] + linevert) * 64))] ^= 1;
                        }
                    }
                    if (height == 16)
                    {
                        position++;
                        pixel = memory[position + linevert];
                        for (int linehoriz = 0; linehoriz < 8; linehoriz++)
                        {
                        if ((pixel & (128 >> linehoriz)) != 0)
                        {
                            if (display[(registers[x] + linehoriz + ((registers[y] + linevert) * 64))] == 1)
                            {
                                registers[15] = 1;
                            }
                            display[(registers[x] + linehoriz + ((registers[y] + linevert) * 64))] ^= 1;
                        }
                        }
                    }

                }
                continue;
            }
            if (opone <= 240) //EX9E Skip next instruction if key in VX is pressed
            {
                if (optwo == 0x9E)
                {
                if (false)
                {
                    if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0)))
                    {
                        pc += 2;
                    }
                    pc += 2;
                }
                } else if (false) { //EXA1 Skip next instruction if key in VX is not pressed

                    if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0)))
                    {
                        pc += 2;
                    }
                    pc += 2;
                }
                continue;
            }
            if (optwo == 0x07) //FX07 Set VX to delay timer
            {
                registers[x] = delaytimer;
                continue;
            }
            if (optwo == 0x15)//FX15
            {
                delaytimer = registers[x];
                continue;
            }
            if (optwo == 0x18) //FX18
            {
                soundtimer = registers[x];
                continue;
            }
            if (optwo == 0x1E) //FX1E
            {
                i = (i + registers[x]) % 65536;
                continue;
            }
            if (optwo == 0x29) //FX29 Locate font
            {
                i = 5 * registers[x];
                continue;
            }
            if (optwo == 0x33) //FX33 Store BCD of VX in I, I+1, I+2
            {
                memory[i] = registers[x] / 100;
                memory[i+1] = (registers[x] / 10) % 10;
                memory[i+2] = registers[x] % 10;
                continue;
            }
            if (optwo == 0x55) //FX55 Store V0 to VX in memory starting at I
            {
                int dummy;
                for (dummy = 0; dummy <= x; dummy++)
                {
                    memory[i + dummy] = registers[dummy];
                }
                i = (i + dummy) % 65536;
                continue;
            }
            if (optwo == 0x65) //FX65 Load V0 to VX from memory starting at I
            {
                int dummy;
                for (dummy = 0; dummy <= x; dummy++)
                {
                    registers[dummy] = memory[i + dummy];
                }
                i = (i + dummy) % 65536;
                continue;
            }

            
            
        }
        //Tick timers
        if (delaytimer >= 1)
        {
            delaytimer--;
        }
        if (soundtimer >= 1)
        {
            soundtimer--;
        }        
        //Display
                    //System.out.print("\033c");            
                    System.out.println();
                    int dummy = 0;
                    for (int height = 1; height <= 32; height++)
                    {
                        for (int width = 1; width <= 64; width++)
                        {
                            
                            if (display[dummy] == 1) {System.out.print("█");} else {System.out.print(" ");}; //display 1 pixel from the screen buffer
                            System.out.print(" "); //display 1 pixel from the screen buffer
                            dummy++;
                        }
                        System.out.println();
                    }
        }
    }
    
    
    
    
}
