unit CommentsTest;

interface

uses
  Windows;

{** documented class **}
type
  TCommentsTest = class(TForm)
  public
    
    {*** documented public procedure ***}
    procedure foo11;
    
    //undocumented public procedure
    procedure foo22;
  private
  	field1: integer;
  protected
    { comment line }
    //another comment
    (* block comment *)
  end;

var
  window: TMainWindow;

(** block comment, empty comment lines below (3)



blah blah **)

{ comment // blah (* blah
 *)) }

(** { blah 
} **)

{  } implementation {  }

{$R *.dfm}

end.