unit Constructors;

interface

type
  TFoo = class(TObject)
  public
    procedure Proc;
  end;

  TBar = class(TFoo)
  public
    procedure Proc; override;
  end;

implementation

procedure Test;
var
  Foo: TFoo;
  Bar: TBar;
begin
  TFoo.Create.Proc;
  TBar.Create.Proc;
end;

end.