package jigglybot;

import discord4j.core.object.entity.Member;
import jigglybot.dialog.DialogPickNickname;
import jigglybot.item.Item;
import jigglybot.monster.Monster;
import jigglybot.monster.Species;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class UserWrapper implements ICanBattle
{
    public static HashMap<Long, UserWrapper> wrappers = new HashMap<>();
    public static final String save_dir = "userdata";
    public static final int entries_per_page = 20;

    public long id;
    public String name;
    public int page = 0;

    public Monster[] squad = new Monster[6];
    public ArrayList<Monster> storage = new ArrayList<>();
    public boolean initialized = false;
    public boolean inBattle = false;

    public int[] dex = new int[Species.currentID];

    public ArrayList<Item> items = new ArrayList<>();

    public UserWrapper(long id)
    {
        //items.add(new Item())
        this.id = id;

        this.dex[Species.by_name.get("bulbasaur").id] = 1;
        this.dex[Species.by_name.get("charmander").id] = 1;
        this.dex[Species.by_name.get("squirtle").id] = 1;
        this.dex[Species.by_name.get("pikachu").id] = 1;

        this.load();
    }

    public static UserWrapper get(Member m)
    {
        long l = m.getId().asLong();
        UserWrapper w = wrappers.get(l);

        if (w == null)
        {
            w = new UserWrapper(l);
            wrappers.put(l, w);
        }

        w.name = m.getDisplayName();

        return w;
    }

    public void pickStarter(ChannelWrapper c, Species s)
    {
        this.initialized = true;
        Monster m = new Monster(s, 5);
        m.setOwner(this);
        c.messageChannel.createMessage("Congratulations! You have received " + m.getName() + "!").block();
        c.currentDialog = new DialogPickNickname(c, this, m);
        c.currentDialog.execute();
    }

    public void pickStarterAsync(ChannelWrapper c, Species s)
    {
        this.initialized = true;
        Monster m = new Monster(s, 5);
        m.setOwner(this);
        
        // Set a proper name (Pokemon names are usually uppercase in this game)
        m.name = s.name.toUpperCase();
        
        // Add to squad
        this.squad[0] = m;
        
        // Save the game
        this.save();
    }

    public void reset()
    {
        // Clear all Pokemon
        for (int i = 0; i < this.squad.length; i++) {
            this.squad[i] = null;
        }
        this.storage.clear();
        
        // Reset progress
        this.initialized = false;
        this.inBattle = false;
        this.page = 0;
        
        // Clear Pokedex
        for (int i = 0; i < this.dex.length; i++) {
            this.dex[i] = 0;
        }
        
        // Reset starter visibility in Pokedex
        this.dex[Species.by_name.get("bulbasaur").id] = 1;
        this.dex[Species.by_name.get("charmander").id] = 1;
        this.dex[Species.by_name.get("squirtle").id] = 1;
        this.dex[Species.by_name.get("pikachu").id] = 1;
        
        // Clear items
        this.items.clear();
        
        // Save the reset state
        this.save();
        
        // Also delete the save file for a complete reset
        if (this.getFile().exists()) {
            this.getFile().delete();
        }
    }

    @Override
    public Monster getNextMonster()
    {
        for (int i = 0; i < this.squad.length; i++)
        {
            if (this.squad[i] != null && this.squad[i].hp > 0)
                return this.squad[i];
        }

        return null;
    }

    @Override
    public void queryMove(ChannelWrapper cw, Monster m, Monster enemy)
    {
        StringBuilder s = new StringBuilder();

        s.append(m.getDisplayString(0).substring(3)).append("    vs. ").append(enemy.getDisplayString(0).substring(3));

        s.append("\nWhat will ").append(m.getName()).append(" do?\n").append("```\nFIGHT\n");

        for (int i = 0; i < m.moves.length; i++)
        {
            if (m.moves[i] != null)
                s.append(i + 1).append(". ").append(m.moves[i].name).append(" (PP ").append(m.movePP[i]).append("/").append(m.moves[i].maxPP).append(")\n");
        }

        s.append("Psst... Looking for more moves? Use -LEARN <move name> to learn that move!``````\nITEM\nNot yet available! Use -CATCH for now!``````\nSWITCH POKéMON\n");

        for (int i = 0; i < this.squad.length; i++)
        {
            if (this.squad[i] != null)
            {
                s.append(this.squad[i].getDisplayString(i));
            }
        }

        s.append("``````\nRUN\nAttempts to escape the battle!```");

        cw.queue(s.toString());
        cw.advance();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    public boolean save()
    {
        try
        {
            File f = this.getFile();
            
            // Ensure the parent directory exists
            File parentDir = f.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    System.err.println("Warning: Failed to create userdata directory: " + parentDir.getAbsolutePath());
                }
            }

            if (!f.exists())
                f.createNewFile();

            PrintWriter pw = new PrintWriter(f);

            pw.println("squad");

            for (Monster m: this.squad)
            {
                if (m != null)
                    pw.println(m.toString());
            }

            pw.println("storage");

            for (Monster m: this.storage)
            {
                pw.println(m.toString());
            }

            pw.println("dex");

            StringBuilder s = new StringBuilder();
            for (int i = 0; i < this.dex.length; i++)
            {
                s.append(this.dex[i]).append(",");
            }

            pw.println(s.substring(0, s.length() - 1));

            pw.close();

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void load()
    {
        if (!this.getFile().exists())
            return;

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(this.getFile()));

            int monsterCount = 0;
            int mode = 0;
            String line = br.readLine();

            while (line != null)
            {
                if (line.equals("squad"))
                    mode = 1;
                else if (line.equals("storage"))
                    mode = 2;
                else if (line.equals("dex"))
                    mode = 3;
                else
                {
                    if (mode == 1)
                    {
                        this.squad[monsterCount] = new Monster(line, this);
                        monsterCount++;
                        this.initialized = true;
                    }
                    else if (mode == 2)
                    {
                        this.storage.add(new Monster(line, this));
                    }
                    else if (mode == 3)
                    {
                        String[] s = line.split(",");

                        for (int i = 0; i < s.length; i++)
                        {
                            this.dex[i] = Integer.parseInt(s[i]);
                        }
                    }
                }

                line = br.readLine();
            }

            br.close();

            for (Monster m : this.squad)
            {
                if (m != null)
                    this.dex[m.species.id] = 2;
            }

            for (Monster m : this.storage)
            {
                if (m != null)
                    this.dex[m.species.id] = 2;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void endBattle()
    {
        this.save();

        for (Monster m: this.squad)
        {
            if (m == null)
                continue;

            for (int i = 0; i < m.stages.length; i++)
            {
                m.stages[i] = 0;
            }

            m.confuseTurns = 0;

            if (m.status == Monster.asleep)
                m.sleepTurns = (int) (Math.random() * 7 + 1);

            m.flinched = false;
        }
    }

    public File getFile()
    {
        return new File(save_dir + "/" + this.id);
    }

    public void printMonsters(ChannelWrapper setting)
    {
        setting.messageChannel.createMessage(this.getMonstersString(setting)).block();
    }

    public String getMonstersString(ChannelWrapper setting)
    {
        StringBuilder s = new StringBuilder("```Your POKéMON:\n");

        for (int i = 0; i < this.squad.length; i++)
        {
            if (this.squad[i] != null)
                s.append(this.squad[i].getDisplayString(i));
        }

        boolean pcAccess = setting.location.hasCenter && !inBattle;

        if (pcAccess)
            s.append("You can DEPOSIT or WITHDRAW POKéMON into the STORAGE PC!");

        if (this.storage.size() > 0)
        {
            if (!pcAccess)
                s.append("You also have ").append(this.storage.size()).append(" other POKéMON in STORAGE! (Access STORAGE PC from a POKéMON CENTER!)");
            else
            {
                s.append("``````Your POKéMON storage PC:\n");

                for (int i = this.page * entries_per_page; i < Math.min(this.storage.size(), (this.page + 1) * entries_per_page); i++)
                {
                    Monster mon = this.storage.get(i);

                    if (mon != null)
                        s.append(mon.getDisplayString(i));
                }

                if (this.storage.size() > entries_per_page)
                    s.append("PAGE ").append(this.page + 1).append(" of ").append((this.storage.size() - 1) / entries_per_page + 1).append("\n");

                s.append("You can also RELEASE POKéMON from the STORAGE PC!");
            }
        }

        s.append("```");

        return s.toString();
    }
}
