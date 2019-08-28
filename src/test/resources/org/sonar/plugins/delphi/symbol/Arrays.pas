unit Arrays;

{This is a sample Delphi file.}

interface

type
  TBar = class(TObject)
  public
    procedure Proc;
  end;

  TBaz = class(TObject)
  private
    FBar: TBar;
  public
    property DefaultProperty[Index: Integer]: TBar read FBar; default;
  end;

  TFoo = class(TObject)
  private
    FBar: TBar;
    FBaz: TBaz;
  public
    property ArrayProperty[Index: Integer]: TBar read FBar; 
    property DefaultProperty[Index: Integer]: TBar read FBar; default;
    property DefaultProperty[Index: Integer; Index2: Integer]: TBaz read FBaz; default;
  end;

implementation

procedure TBar.Proc;
begin
  // Do nothing
end;

procedure Test(Foo: TFoo);
var
  SimpleArray: array of TBar;
  MultiDimensionalArray: array[0..1, 0..1] of TBar;
begin
  SimpleArray[0].Proc;
  MultiDimensionalArray[0, 0].Proc;
  MultiDimensionalArray[0][0].Proc;
  Foo.ArrayProperty[0].Proc;
  Foo.DefaultProperty[0].Proc;
  Foo[0].Proc;
  Foo.DefaultProperty[0,0].DefaultProperty[0].Proc;
  Foo[0,0][0].Proc;
end;

end.