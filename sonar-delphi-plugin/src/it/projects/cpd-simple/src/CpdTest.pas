unit CpdTest;

interface

//testDefinitionsIncludes
{ testDefinitionsIncludes }
(* testDefinitionsIncludes *)

type
  TfDemo = class(TForm)
    bShowTracker: TButton;
    procedure bShowTrackerClick(Sender: TObject);
  private
  //testDefinitionsIncludes
  public
  {testDefinitionsIncludes}
  end;
  
type
  TfDemoSecond = class (TForm)
    bShowTracker: TButton;
    procedure bShowTrackerClick(Sender: TObject);
  private

  public

  end;
  
var
  fDemo: TfDemo;

implementation

procedure TfDemo.bShowTrackerClick(Sender: TObject);
begin
  (* long
  multiline
  comment
  *)
  
  for i:=0 to 100 do
  begin
    a := b;
    if b <> c then begin
      b := c;
    end;
    c := d;
    a := b;
    if a < b then begin
      c := b;
    end;
  end;

  for i:=0 to 100 do
  begin
    a := b;
    if b <> c then begin
      b := c;
    end;
    c := d;
    a := b;
    if a < b then begin
      c := b;
    end;
  end;
  
end;

end.